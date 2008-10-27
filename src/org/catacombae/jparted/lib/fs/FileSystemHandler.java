/*-
 * Copyright (C) 2008 Erik Larsson
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catacombae.jparted.lib.fs;

import java.util.LinkedList;
import org.catacombae.hfsexplorer.Util;

/**
 *
 * @author erik
 */
public abstract class FileSystemHandler {
    
    /**
     * Lists all entries present under the <code>path</code> supplied. Path must
     * point to a folder, and is composed of a variable arguments list with each
     * pathname component as a separate String.<br>
     * Invoking this method with no arguments gives the file list of the
     * root directory of the file system.<br>
     * Examples:
     * <ul>
     * <li>
     * Getting the contents of <code>/usr/bin</code> in a UNIX-style filesystem:
     * <code>listFiles("usr", "bin");</code></li>
     * <li>
     * Getting the contents of <code>\Windows\System32</code> in a Windows-style
     * file system: <code>listFiles("Windows", "System32");</code>
     * </li>
     * <li>
     * Getting the contents of <code>Users:joe</code> in an old Macintosh-style
     * file system: <code>listFiles("Users", "joe");</code>
     * </li>
     * </ul>
     * 
     * @param path the path to the requested folder with each path component
     * as a separate string. The first component under the root dir will be
     * leftmost in the argument list.
     * @return an array with file system entries that represents the contents of
     * the requested folder, or <code>null</code> if the folder can't be found.
     */
    public abstract FSEntry[] list(String... path);
    
    /**
     * Returns the root folder of the file system hierarchy.
     * 
     * @return the root folder of the file system hierarchy.
     */
    public abstract FSFolder getRoot();

    /**
     * Returns the FSEntry present on the location <code>path</code>. If the
     * path is invalid in this file system, <code>null</code> is returned.
     *
     * @param path the file system path to the requested entry, path element by
     * path element (ex. <code>getEntry("usr", "local", "bin", "java");</code>).
     * @return the FSEntry present on the location <code>path</code>, or
     * <code>null</code> if no such entry exists.
     */
    public abstract FSEntry getEntry(String... path);
    
    /**
     * Looks up the FSEntry denoted by the supplied POSIX path. Since POSIX
     * paths may be relative, a root folder is needed to resolve the relative
     * path structure. If the POSIX pathname is absolute, the root folder
     * parameter will not be used.
     *
     * @param posixPath the POSIX pathname.
     * @param rootFolder the root folder from which we should start resolving
     * the path.
     * @return the FSEntry corresponding to the supplied POSIX path, or <code>
     * null</code> if no such pathname could be found.
     * @throws java.lang.IllegalArgumentException if <code>posixPath</code> is
     * an invalid pathname.
     */
    public FSEntry getEntryByPosixPath(final String posixPath,
            final String... rootFolderPath) throws IllegalArgumentException {
        final String prefix = globalPrefix;
        globalPrefix += "    ";
        try {
            System.err.println(prefix + "getEntryByPosixPath(" + posixPath + ", " + Util.concatenateStrings(rootFolderPath, "/") + ");");
            String[] path = getTruePathFromPosixPath(posixPath, rootFolderPath);

            if(path != null) {
                System.err.println(prefix + "  getEntryByPosixPath: path = " + Util.concatenateStrings(path, "/"));
                return getEntry(path);
            }
            else
                return null;
        } finally { System.err.println(prefix + "Returning from getEntryByPosixPath"); globalPrefix = prefix; }
    }

    public String globalPrefix = "";
    public String[] getTruePathFromPosixPath(final String posixPath,
            final String... rootFolderPath) throws IllegalArgumentException {
        final String prefix = globalPrefix;
        globalPrefix += "    ";
        System.err.println(prefix + "getTruePathFromPosixPath(\"" + posixPath + "\", { \"" +
                Util.concatenateStrings(rootFolderPath, "\", \"") + "\" });");
        try {
        String[] components = posixPath.split("/");

        int i = 0;
        //FSEntry curEntry;
        LinkedList<String> pathStack = new LinkedList<String>();
        LinkedList<String[]> visitedLinks = null;

        // If we encounter a '/' as the first character, we have an absolute path
        if(posixPath.startsWith("/")) {
            i = 1;
        }
        else {
            for(String pathComponent : rootFolderPath)
                pathStack.addLast(pathComponent);
        }

        FSEntry curEntry2 = null;

        for(; i < components.length; ++i) {
            String[] curPath = pathStack.toArray(new String[pathStack.size()]);
            System.err.println(prefix + "  gtpfpp: curPath=\"" + Util.concatenateStrings(curPath, "\", \"") + "\"");
            if(curEntry2 == null) {
                curEntry2 = getEntry(curPath);
                System.err.println(prefix + "  gtpfpp: curEntry2=" + curEntry2);
            }
            
            FSFolder curFolder;
            if(curEntry2 instanceof FSFolder)
                curFolder = (FSFolder) curEntry2;
            else if(curEntry2 instanceof FSLink) {
                FSLink curLink = (FSLink) curEntry2;
                System.err.println(prefix + "  gtpfpp: It was a link!");
                // Resolve links.
                if(visitedLinks == null)
                    visitedLinks = new LinkedList<String[]>();
                else
                    visitedLinks.clear();

                FSEntry linkTarget = null;
                String[] curLinkPath = curPath;
                while(curLinkPath != null) {
                    visitedLinks.add(curLinkPath);
                    String[] parentPath =
                            Util.arrayCopy(curLinkPath, 0, new String[curLinkPath.length-1], 0, curLinkPath.length-1);
                    System.err.println(prefix + "  gtpfpp:   Resolving link target against " + Util.concatenateStrings(parentPath, "/"));
                    linkTarget =
                            curLink.getLinkTarget(parentPath);

                    System.err.println(prefix + "  gtpfpp:   Result: " + linkTarget);

                    if(linkTarget != null && linkTarget instanceof FSLink) {
                        curLink = (FSLink) linkTarget;
                        curLinkPath = getTargetPath(curLink, parentPath);

                        // Check the visited list to see if we have been here before.
                        for(String[] visitedPath : visitedLinks) {
                            if(curLinkPath.length == visitedPath.length) {
                                int j = 0;
                                for(; j < curLinkPath.length; ++j) {
                                    if(!curLinkPath[j].equals(visitedPath[j]))
                                        break;
                                }
                                if(j == curLinkPath.length)
                                    return null; // We have been here before! Circular linking...
                            }
                        }
                    }
                    else
                        curLinkPath = null;
                }

                System.err.println(prefix + "  gtpfpp: Before test.");
                if(linkTarget == null)
                    return null; // Invalid link target.
                if(linkTarget instanceof FSFolder)
                    curFolder = (FSFolder) linkTarget;
                else if(linkTarget instanceof FSFile)
                    return null; // Invalid intermediate path component
                else
                    throw new RuntimeException("Unknown type: " + linkTarget.getClass());

                visitedLinks.clear();
            }
            else
                return null; // Invalid pathname

            String curPathComponent = components[i];
            System.err.println(prefix + "  gtpfpp: curPathComponent=" + curPathComponent);

            if(curPathComponent.length() == 0 || curPathComponent.equals(".")) {
                // We allow empty components (multiple slashes between components)
            }
            else if(curPathComponent.equals("..")) {
                pathStack.removeLast();
                curEntry2 = null; // Triggers a parse from dir stack
            }
            else {
                String fsPathnameComponent = parsePosixPathnameComponent(curPathComponent);
                System.err.println(prefix + "  gtpfpp: fsPathnameComponent=" + fsPathnameComponent);

                FSEntry nextEntry = null;
                for(FSEntry entry : curFolder.list()) {
                    System.err.println(prefix + "  gtpfpp:   Checking if " + entry.getName() + " matches...");

                    if(entry.getName().equals(fsPathnameComponent)) {
                        nextEntry = entry;
                        System.err.println(prefix + "  gtpfpp:     Match found!!");
                        break;
                    }
                }

                System.err.println(prefix + "  gtpfpp: nextEntry=" + nextEntry);
                if(nextEntry != null) {
                    curEntry2 = nextEntry;
                    pathStack.add(nextEntry.getName());
                }
                else
                    return null; // Invalid pathname
            }
        }

        final String[] res = pathStack.toArray(new String[pathStack.size()]);
        System.err.println(prefix + "  gtpfpp: Returning " + Util.concatenateStrings(res, "/"));
        return res;
        } finally { System.err.println(prefix + "Returning from getTruePathFromPosixPath."); globalPrefix = prefix; }
    }

    /**
     * Converts the supplied POSIX pathname component into the proper file
     * system pathname component form. For example, the ':' character in HFS+
     * POSIX pathname components represent the character '/' in the file system.
     * <br>
     * For a strictly POSIX file system, this method should just bounce the
     * input string.
     *
     * @param posixPathnameComponent
     * @return
     */
    public abstract String parsePosixPathnameComponent(String posixPathnameComponent);

    /**
     * Converts the supplied file system pathname component into a corresponding
     * POSIX pathname component. This may involve converting certain
     * POSIX-incompatible characters into suitable replacements, such as
     * pathname components containing the character '/'.
     * <br>
     * For a strictly POSIX file system, this method should just bounce the
     * input string.
     *
     * @param fsPathnameComponent
     * @return
     */
    public abstract String generatePosixPathnameComponent(String fsPathnameComponent);

    /**
     * Returns path to the link's target in absolute form.
     * @param link
     * @return
     */
    public abstract String[] getTargetPath(FSLink link, String[] parentDir);

    /**
     * Returns the predefined fork types that this file system recognizes and
     * supports. Note that this does not mean that every file in the file system
     * will have these fork types, it just means that these forks may be present
     * in any file. Any fork type not returned by this method is unknown to the
     * file system handler and has no meaning in its context.<br>
     * <b>Note:</b> All implementations must support the FSForkType.DATA type,
     * as the main fork, the data fork, must always be defined for a file.
     * 
     * @return the predefined fork types that this file system recognizes and
     * supports.
     */
    public abstract FSForkType[] getSupportedForkTypes();
    
    /**
     * Closes the file system handler and frees allocated resources.
     */
    public abstract void close();
}
