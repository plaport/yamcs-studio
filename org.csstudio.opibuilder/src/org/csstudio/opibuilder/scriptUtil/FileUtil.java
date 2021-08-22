/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.scriptUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.widgetActions.OpenFileAction;
import org.csstudio.opibuilder.widgetActions.OpenWebpageAction;
import org.csstudio.opibuilder.widgetActions.PlayWavFileAction;
import org.csstudio.ui.util.dialogs.ResourceSelectionDialog;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * The Utility class to help file operating.
 * 
 * @author Xihui Chen
 *
 */
public class FileUtil {

    /**
     * Load the root element of an XML file. The element is a JDOM Element.
     * 
     * @param filePath
     *            path of the file. It must be an absolute path which can be either<br>
     *            a workspace path such as <code>/BOY Examples/Scripts/myfile.xml</code><br>
     *            a local file system path such as <code>C:\myfile.xml</code><br>
     *            or an URL path such as <code>http://mysite.com/myfile.xml</code>. *
     * @return root element of the XML file.
     * @throws Exception
     *             if the file does not exist or is not a correct XML file.
     */
    public static Element loadXMLFile(String filePath) throws Exception {
        return loadXMLFile(filePath, null);
    }

    /**
     * Load the root element of an XML file. The element is a JDOM Element.
     * 
     * @param filePath
     *            path of the file. It can be an absolute path or a relative path to the OPI that contains the specified
     *            widget. If it is an absolute path, it can be either<br>
     *            a workspace path such as <code>/BOY Examples/Scripts/myfile.xml</code><br>
     *            a local file system path such as <code>C:\myfile.xml</code><br>
     *            or an URL path such as <code>http://mysite.com/myfile.xml</code>.
     * @param widget
     *            a widget in the OPI, which is used to provide relative path reference. It can be null if the path is
     *            an absolute path.
     * @return root element of the XML file.
     * @throws Exception
     *             if the file does not exist or is not a correct XML file.
     */
    public static Element loadXMLFile(String filePath, AbstractBaseEditPart widget) throws Exception {
        IPath path = buildAbsolutePath(filePath, widget);
        SAXBuilder saxBuilder = new SAXBuilder();
        saxBuilder.setEntityResolver(new CssEntityResolver());
        File file = ResourceUtil.getFile(path);
        Document doc;
        if (file == null) {
            InputStream inputStream = ResourceUtil.pathToInputStream(path);
            doc = saxBuilder.build(inputStream);
            inputStream.close();
        } else {
            doc = saxBuilder.build(file);
        }
        return doc.getRootElement();
    }

    /**
     * Return an {@link InputStream} of the file on the specified path. The client is responsible for closing the stream
     * when finished.
     * 
     * @param filePath
     *            path of the file. It can be an absolute path or a relative path to the OPI that contains the specified
     *            widget. If it is an absolute path, it can be either<br>
     *            a workspace path such as <code>/BOY Examples/Scripts/myfile.xml</code><br>
     *            a local file system path such as <code>C:\myfile.xml</code><br>
     *            or an URL path such as <code>http://mysite.com/myfile.xml</code>.
     * @param widget
     *            a widget in the OPI, which is used to provide relative path reference. It can be null if the path is
     *            an absolute path.
     * @return java.io.InputStream of the file.
     * @throws Exception
     *             if the file does not exist.
     */
    public static InputStream getInputStreamFromFile(String filePath, AbstractBaseEditPart widget) throws Exception {
        IPath path = buildAbsolutePath(filePath, widget);
        return ResourceUtil.pathToInputStream(path);
    }

    public static InputStream getInputStreamFromFile(String filePath) throws Exception {
        return getInputStreamFromFile(filePath, null);
    }

    /**
     * Read a text file.
     * 
     * @param filePath
     *            path of the file. It must be an absolute path which can be either<br>
     *            a workspace path such as <code>/BOY Examples/Scripts/myfile.xml</code><br>
     *            a local file system path such as <code>C:\myfile.xml</code><br>
     *            or an URL path such as <code>http://mysite.com/myfile.xml</code>.
     * @return a string of the text.
     * @throws Exception
     *             if the file does not exist or is not a correct text file.
     */
    public static String readTextFile(String filePath) throws Exception {
        return readTextFile(filePath, null);
    }

    /**
     * Read a text file.
     * 
     * @param filePath
     *            path of the file. It can be an absolute path or a relative path to the OPI that contains the specified
     *            widget. If it is an absolute path, it can be either<br>
     *            a workspace path such as <code>/BOY Examples/Scripts/myfile.xml</code><br>
     *            a local file system path such as <code>C:\myfile.xml</code><br>
     *            or an URL path such as <code>http://mysite.com/myfile.xml</code>.
     * @param widget
     *            a widget in the OPI, which is used to provide relative path reference. It can be null if the path is
     *            an absolute path.
     * @return a string of the text.
     * @throws Exception
     *             if the file does not exist or is not a correct text file.
     */
    public static String readTextFile(String filePath, AbstractBaseEditPart widget) throws Exception {
        InputStream inputStream = getInputStreamFromFile(filePath, widget);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder sb = new StringBuilder();

        try {
            String s;
            while ((s = reader.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }
        } finally {
            reader.close();
            inputStream.close();
        }
        return sb.toString();
    }

    /**
     * Write a text file.
     * 
     * @param filePath
     *            path of the file. It must be an absolute path which can be either<br>
     *            a workspace path such as <code>/BOY Examples/Scripts/myfile.xml</code><br>
     *            or a local file system path such as <code>C:\myfile.xml</code>.
     * @param inWorkspace
     *            true if the file path is a workspace file path. Otherwise, it will be recognized as a local file
     *            system file.
     * @param text
     *            the text to be written to the file.
     * @param append
     *            true if the text should be appended to the end of the file.
     * @throws Exception
     *             if error happens.
     */
    public static void writeTextFile(String filePath, boolean inWorkspace,
            String text, boolean append) throws Exception {
        writeTextFile(filePath, inWorkspace, null, text, append);
    }

    /**
     * Write a text file.
     * 
     * @param filePath
     *            path of the file. It can be an absolute path or a relative path to the OPI that contains the specified
     *            widget. If it is an absolute path, it can be either<br>
     *            a workspace path such as <code>/BOY Examples/Scripts/myfile.xml</code><br>
     *            a local file system path such as <code>C:\myfile.xml</code><br>
     *            or an URL path such as <code>http://mysite.com/myfile.xml</code>.
     * @param inWorkspace
     *            true if the file path is a workspace file path. Otherwise, it will be recognized as a local file
     *            system file.
     * @param widget
     *            a widget in the OPI, which is used to provide relative path reference. It can be null if the path is
     *            an absolute path.
     * @param text
     *            the text to be written to the file.
     * @param append
     *            true if the text should be appended to the end of the file.
     * @throws Exception
     *             if error happens.
     */
    public static void writeTextFile(String filePath, boolean inWorkspace,
            AbstractBaseEditPart widget, String text,
            boolean append) throws Exception {
        IPath path = FileUtil.buildAbsolutePath(filePath, widget);
        if (inWorkspace) {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IWorkspaceRoot root = workspace.getRoot();
            String projectName = path.segment(0);
            IProject project = root.getProject(projectName);
            if (!(project.exists())) {
                project.create(new NullProgressMonitor());
            }
            project.open(new NullProgressMonitor());
            IFolder folder = null;
            for (int i = 1; i < path.segmentCount() - 1; i++) {
                if (i == 1) {
                    folder = project.getFolder(path.segment(i));
                } else {
                    folder = folder.getFolder(path.segment(i));
                }
                if (!(folder.exists())) {
                    folder.create(true, true, null);
                }
            }
            IContainer container;
            if (folder == null) {
                container = project;
            } else {
                container = folder;
            }
            IFile file = container.getFile(ResourceUtil.getPathFromString(path.lastSegment()));
            if (file.exists()) {
                StringBuilder sb = new StringBuilder();
                if (append) {
                    sb.append(FileUtil.readTextFile(filePath, widget));
                }
                sb.append(text);
                file.setContents(
                        new ByteArrayInputStream(sb.toString().getBytes("UTF-8")), true, false, null);
            } else {
                File sysFile = file.getLocation().toFile();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(sysFile, append), "UTF-8"));
                writer.write(text);
                writer.flush();
                writer.close();
            }
        } else {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(path.toString(), append), "UTF-8"));
            writer.write(text);
            writer.flush();
            writer.close();
        }
    }

    /**
     * Open a file in default editor. If no such an editor for the type of file, OS default program will be called to
     * open this file.
     * 
     * @param filePath
     *            path of the file. It can be an absolute path or a relative path to the OPI that contains the specified
     *            widget. If it is an absolute path, it can be either a workspace path such as <br>
     *            <code>/BOY Examples/Scripts/myfile.txt</code><br>
     *            or a local file system path such as <code>C:\myfile.txt</code>.
     * @param widget
     *            a widget in the OPI, which is used to provide relative path reference. It can be null if the path is
     *            an absolute path.
     */
    public static void openFile(String filePath, AbstractBaseEditPart widget) {
        OpenFileAction action = new OpenFileAction();
        action.setWidgetModel(widget != null ? widget.getWidgetModel() : null);
        action.setPropertyValue(OpenFileAction.PROP_PATH, filePath);
        action.run();
    }

    public static void openFile(String filePath) {
        openFile(filePath, null);
    }

    /**
     * Open a web page.
     * 
     * @param link
     *            link to the web page. It can be a link started with http://, https:// or file://.
     */
    public static void openWebPage(String link) {
        OpenWebpageAction action = new OpenWebpageAction();
        action.setPropertyValue(OpenWebpageAction.PROP_HYPERLINK, link);
        action.run();
    }

    /**
     * Play a .wav file.
     * 
     * @param filePath
     *            path of the file. It can be an absolute path or a relative path to the OPI that contains the specified
     *            widget. If it is an absolute path, it can be either<br>
     *            a workspace path such as <code>/BOY Examples/Scripts/myfile.xml</code><br>
     *            a local file system path such as <code>C:\myfile.xml</code><br>
     *            or an URL path such as <code>http://mysite.com/myfile.xml</code>.
     * @param widget
     *            a widget in the OPI, which is used to provide relative path reference. It can be null if the path is
     *            an absolute path.
     */
    public static void playWavFile(String filePath, AbstractBaseEditPart widget) {
        PlayWavFileAction action = new PlayWavFileAction();
        action.setWidgetModel(widget != null ? widget.getWidgetModel() : null);
        action.setPropertyValue(PlayWavFileAction.PROP_PATH, filePath);
        action.run();
    }

    public static void playWavFile(String filePath) {
        playWavFile(filePath, null);
    }

    /**
     * Open a file select dialog.
     * 
     * @param inWorkspace
     *            true if it is a workspace file dialog; Otherwise, it is a local file system file dialog.
     * @return the full file path. Or null if it is cancelled.
     */
    public static String openFileDialog(boolean inWorkspace) {
        if (inWorkspace) {
            ResourceSelectionDialog rsd = new ResourceSelectionDialog(
                    Display.getCurrent().getActiveShell(), "Select File", new String[] { "*" });
            if (rsd.open() == Window.OK) {
                if (rsd.getSelectedResource() != null) {
                    return rsd.getSelectedResource().toString();
                }
            }
        } else {
            FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
            return dialog.open();
        }

        return null;
    }

    /**
     * Open a file select dialog.
     * 
     * @param startingFolder
     *            the folder where the file dialog starts.
     * @return the full file path. Or null if it is cancelled.
     */
    public static String openFileDialog(String startingFolder) {
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
        dialog.setFilterPath(startingFolder);
        return dialog.open();
    }

    /**
     * Open a file save dialog.
     * 
     * @param inWorkspace
     *            true if it is a workspace file dialog; Otherwise, it is a local file system file dialog.
     * @return the full file path. Or null if it is cancelled.
     */
    public static String saveFileDialog(boolean inWorkspace) {
        if (inWorkspace) {
            SaveAsDialog rsd = new SaveAsDialog(Display.getCurrent().getActiveShell());
            if (rsd.open() == Window.OK) {
                if (rsd.getResult() != null) {
                    return rsd.getResult().toOSString();
                }
            }
        } else {
            FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
            return dialog.open();
        }

        return null;
    }

    /**
     * Open a file save dialog.
     * 
     * @param startingFolder
     *            the folder where the file dialog starts.
     * @return the full file path. Or null if it is cancelled.
     */
    public static String saveFileDialog(String startingFolder) {
        FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
        dialog.setFilterPath(startingFolder);
        return dialog.open();
    }

    protected static IPath buildAbsolutePath(String filePath, AbstractBaseEditPart widget) {
        IPath path = ResourceUtil.getPathFromString(filePath);
        if (!path.isAbsolute()) {
            path = ResourceUtil.buildAbsolutePath(widget.getWidgetModel(), path);
        }
        return path;
    }

    /**
     * Convert a workspace path to system path. If this resource is a project that does not exist in the workspace, or a
     * file or folder below such a project, this method returns null.
     * 
     * @param workspacePath
     *            path in workspace.
     * @return the system path on OS. Return an empty string if the path doesn't exist.
     */
    public static String workspacePathToSysPath(String workspacePath) {
        IPath path = ResourceUtil.workspacePathToSysPath(new Path(workspacePath));
        if (path == null) {
            return null;
        }
        return path.toOSString();
    }
}
