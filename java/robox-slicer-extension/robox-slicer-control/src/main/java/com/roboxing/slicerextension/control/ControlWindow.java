/*
 * This file is part of Robox Slicer Extension.
 *
 * Robox Slicer Extension is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robox Slicer Extension is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robox Slicer Extension.  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package com.roboxing.slicerextension.control;

import static javax.swing.SpringLayout.EAST;
import static javax.swing.SpringLayout.NORTH;
import static javax.swing.SpringLayout.SOUTH;
import static javax.swing.SpringLayout.WEST;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Controller window
 *
 * @author Daniel Sendula
 */
public class ControlWindow extends JFrame {

    private static final Slicer[] SLICERS = {
            new Slicer("Default AM Cura", "DefaultAMCura", true),
            new Slicer("Slic3r", "Slic3r", true),
            new Slicer("Simplify 3D", "Simplify3D", false),
            new Slicer("Cura 2.5", "Cura20", false),
            new Slicer("Cura 3.0", "Cura30", false)
    };

    private static final Script[] SCRIPTS = {
            new Script("Internal"),
            new Script("External")
    };

    private JPanel buttonsPanel;
    private JButton saveButton;
    private JButton cancelButton;

    private JPanel informationPanel;

    private JPanel mainPanel;

    private JComboBox<Slicer> slicersDropDown;
    private JComboBox<Script> preProcessorScriptDropDown;
    private JComboBox<Script> postProcessorScriptDropDown;

    private JTextField preProcessorPath;
    private JLabel preProcessorPathError;
    private JButton preProcessorBrowseButton;

    private JTextField postProcessorPath;
    private JLabel postProcessorPathError;
    private JButton postProcessorBrowseButton;

    private JTextArea logArea;

    public ControlWindow() {
        super("Robox Slicer Extension");

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);

        setSize(640, 400);

        createInformationPanel();
        createButtonsPanel();
        createMainPanel();
    }

    public void setLeaveAction(Runnable action) {
        cancelButton.addActionListener(e -> action.run());
        addWindowListener(new WindowListener() {
            @Override public void windowOpened(WindowEvent e) { }
            @Override public void windowIconified(WindowEvent e) { }
            @Override public void windowDeiconified(WindowEvent e) { }
            @Override public void windowDeactivated(WindowEvent e) { }
            @Override public void windowClosed(WindowEvent e) { }
            @Override public void windowActivated(WindowEvent e) { }

            @Override public void windowClosing(WindowEvent e) {
                action.run();
            }
        });
    }

    public void setSaveButtonEnable(boolean enable) {
        saveButton.setEnabled(enable);
    }

    public void setSaveAction(Runnable action) {
        saveButton.addActionListener(e -> action.run());
    }

    public Slicer getSelectedSlicer() {
        return (Slicer)slicersDropDown.getSelectedItem();
    }

    private void createInformationPanel() {
        informationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JLabel label = new JLabel("Robox Slicer Extension");
        label.setFont(label.getFont().deriveFont(label.getFont().getSize() * 2.0f));

        informationPanel.add(label);

        this.add(informationPanel, BorderLayout.NORTH);
    }

    private void createButtonsPanel() {
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        buttonsPanel.add(cancelButton);
        buttonsPanel.add(saveButton);

        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void createMainPanel() {
        mainPanel = new JPanel();
        SpringLayout layout = new SpringLayout();

        mainPanel.setLayout(layout);
        JLabel selectSlicerLabel = new JLabel("Select Slicer:");

        slicersDropDown = new JComboBox<Slicer>(SLICERS) {
            @Override
            public void setSelectedItem(Object item) {
                if (!(item instanceof Slicer) || !((Slicer)item).isEnabled()) {
                    return;
                }
                super.setSelectedItem(item);
            };
        };
        slicersDropDown.setSelectedIndex(0);
        slicersDropDown.setRenderer(new SlicerCellRenderer());

        JLabel gcodePreProcessorLabel = new JLabel("GCode Pre Processor Script:");

        preProcessorScriptDropDown = new JComboBox<Script>(SCRIPTS);
        preProcessorScriptDropDown.setRenderer(new ScriptCellRenderer());
        preProcessorScriptDropDown.setVisible(false);

        preProcessorPath = new JTextField("");
        preProcessorPathError = new JLabel("");
        preProcessorPathError.setForeground(Color.RED);
        preProcessorBrowseButton = new JButton("Browse...");

        JLabel gcodePostProcessorLabel = new JLabel("GCode Post Processor Script:");

        postProcessorScriptDropDown = new JComboBox<Script>(SCRIPTS);
        postProcessorScriptDropDown.setRenderer(new ScriptCellRenderer());
        postProcessorScriptDropDown.setVisible(false);

        postProcessorPath = new JTextField("");
        postProcessorPathError = new JLabel("");
        postProcessorPathError.setForeground(Color.RED);
        postProcessorBrowseButton = new JButton("Browse...");


//        preProcessorPathError.setVisible(false);
//        postProcessorPathError.setVisible(false);


        logArea = new JTextArea("");
        JScrollPane logAreaScroller = new JScrollPane();
        logAreaScroller.setViewportView(logArea);
        logAreaScroller.setBorder(BorderFactory.createTitledBorder("Output"));

        mainPanel.add(selectSlicerLabel);
        mainPanel.add(slicersDropDown);
        mainPanel.add(gcodePreProcessorLabel);
        mainPanel.add(preProcessorScriptDropDown);
        mainPanel.add(preProcessorPath);
        mainPanel.add(preProcessorPathError);
        mainPanel.add(preProcessorBrowseButton);
        mainPanel.add(gcodePostProcessorLabel);
        mainPanel.add(postProcessorScriptDropDown);
        mainPanel.add(postProcessorPath);
        mainPanel.add(postProcessorPathError);
        mainPanel.add(postProcessorBrowseButton);
        mainPanel.add(logAreaScroller);

        layout.putConstraint(NORTH, selectSlicerLabel, 5, NORTH, mainPanel);
        layout.putConstraint(WEST, selectSlicerLabel, 5, WEST, mainPanel);

        layout.putConstraint(NORTH, slicersDropDown, 5, NORTH, mainPanel);
        layout.putConstraint(WEST, slicersDropDown, 5, EAST, selectSlicerLabel);

        layout.putConstraint(SOUTH, selectSlicerLabel, 0, SOUTH, slicersDropDown);


        layout.putConstraint(NORTH, gcodePreProcessorLabel, 5, SOUTH, selectSlicerLabel);
        layout.putConstraint(WEST, gcodePreProcessorLabel, 0, WEST, selectSlicerLabel);
        layout.putConstraint(EAST, selectSlicerLabel, 0, EAST, gcodePreProcessorLabel);

//        layout.putConstraint(NORTH, preProcessorScriptDropDown, 0, NORTH, gcodePreProcessorLabel);
//        layout.putConstraint(WEST, preProcessorScriptDropDown, 5, EAST, gcodePreProcessorLabel);

        layout.putConstraint(NORTH, preProcessorPath, 0, NORTH, gcodePreProcessorLabel);
        layout.putConstraint(WEST, preProcessorPath, 5, EAST, gcodePreProcessorLabel);

        layout.putConstraint(NORTH, preProcessorBrowseButton, 0, NORTH, preProcessorPath);
        layout.putConstraint(EAST, preProcessorPath, 5, WEST, preProcessorBrowseButton);
        layout.putConstraint(EAST, preProcessorBrowseButton, -5, EAST, mainPanel);

        layout.putConstraint(SOUTH, preProcessorPath, 0, SOUTH, preProcessorBrowseButton);
        layout.putConstraint(SOUTH, gcodePreProcessorLabel, 0, SOUTH, preProcessorBrowseButton);

        layout.putConstraint(NORTH, preProcessorPathError, 5, SOUTH, gcodePreProcessorLabel);
        layout.putConstraint(WEST, preProcessorPathError, 0, WEST, gcodePreProcessorLabel);
        layout.putConstraint(EAST, preProcessorPathError, 0, EAST, mainPanel);


        layout.putConstraint(NORTH, gcodePostProcessorLabel, 5, SOUTH, preProcessorPathError);
        layout.putConstraint(WEST, gcodePostProcessorLabel, 0, WEST, gcodePreProcessorLabel);
        layout.putConstraint(EAST, gcodePreProcessorLabel, 0, EAST, gcodePostProcessorLabel);

//        layout.putConstraint(NORTH, postProcessorScriptDropDown, 0, NORTH, gcodePostProcessorLabel);
//        layout.putConstraint(WEST, postProcessorScriptDropDown, 5, EAST, gcodePostProcessorLabel);

        layout.putConstraint(NORTH, postProcessorPath, 5, NORTH, gcodePostProcessorLabel);
        layout.putConstraint(WEST, postProcessorPath, 5, EAST, gcodePostProcessorLabel);

        layout.putConstraint(NORTH, postProcessorBrowseButton, 5, NORTH, gcodePostProcessorLabel);
        layout.putConstraint(EAST, postProcessorPath, 5, WEST, postProcessorBrowseButton);
        layout.putConstraint(EAST, postProcessorBrowseButton, -5, EAST, mainPanel);

        layout.putConstraint(SOUTH, postProcessorPath, 0, SOUTH, postProcessorBrowseButton);
        layout.putConstraint(SOUTH, gcodePostProcessorLabel, 0, SOUTH, postProcessorBrowseButton);

        layout.putConstraint(NORTH, postProcessorPathError, 5, SOUTH, gcodePostProcessorLabel);
        layout.putConstraint(WEST, postProcessorPathError, 0, WEST, gcodePostProcessorLabel);
        layout.putConstraint(EAST, postProcessorPathError, 0, EAST, mainPanel);


        layout.putConstraint(NORTH, logAreaScroller, 15, SOUTH, postProcessorPathError);
        layout.putConstraint(WEST, logAreaScroller, 5, WEST, mainPanel);
        layout.putConstraint(EAST, logAreaScroller, -5, EAST, mainPanel);
        layout.putConstraint(SOUTH, logAreaScroller, -5, SOUTH, mainPanel);

        add(mainPanel, BorderLayout.CENTER);
        preProcessorBrowseButton.addActionListener(e -> invokeFileSelector(preProcessorPath));
        postProcessorBrowseButton.addActionListener(e -> invokeFileSelector(postProcessorPath));
    }

    public void log(String s) {
        Document document = logArea.getDocument();
        try {
            document.insertString(document.getLength(), s + "\n", null);
        } catch (BadLocationException ignore) { }
    }

    public void setPreProcessorScriptPathError(String error) {
        preProcessorPathError.setText(error);
    }

    public void setPreProcessorScriptPath(String path) {
        preProcessorPath.setText(path);
    }

    public String getPreProcessorScriptPath() {
        return preProcessorPath.getText();
    }

    public void setPreProcessorScriptPathChanged(PathChanged callback) {
        setCallbackOnPath(preProcessorPath, callback);
    }

    public void setPostProcessorScriptPathError(String error) {
        postProcessorPathError.setText(error);
    }

    public void setPostProcessorScriptPath(String path) {
        postProcessorPath.setText(path);
    }

    public String getPostProcessorScriptPath() {
        return postProcessorPath.getText();
    }

    public void setPostProcessorScriptPathChanged(PathChanged callback) {
        setCallbackOnPath(postProcessorPath, callback);
    }

    private void setCallbackOnPath(JTextField field, PathChanged callback) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { callback.changed(field.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { callback.changed(field.getText()); }
            @Override public void insertUpdate(DocumentEvent e) { callback.changed(field.getText()); }
        });
    }

    public interface PathChanged { void changed(String s); }

    private class SlicerCellRenderer extends JLabel implements ListCellRenderer<Slicer> {
        public Component getListCellRendererComponent(JList<? extends Slicer> list, Slicer value, int index, boolean isSelected, boolean cellHasFocus) {

            if (!value.isEnabled()) {
                setBackground(UIManager.getColor("ComboBox.disabledBackground"));
                setForeground(UIManager.getColor("ComboBox.disabledForeground"));
            } else if (isSelected) {
                setBackground(UIManager.getColor("ComboBox.selectionBackground"));
                setForeground(UIManager.getColor("ComboBox.selectionForeground"));
            } else {
                setBackground(UIManager.getColor("ComboBox.background"));
                setForeground(UIManager.getColor("ComboBox.foreground"));
            }

            setText(value.getLabel());

            return this;
        }
    }

    private class ScriptCellRenderer extends JLabel implements ListCellRenderer<Script> {
        public Component getListCellRendererComponent(
                JList<? extends Script> list, Script value, int index, boolean isSelected, boolean cellHasFocus) {

            if (isSelected) {
                setBackground(UIManager.getColor("ComboBox.selectionBackground"));
                setForeground(UIManager.getColor("ComboBox.selectionForeground"));
            } else {
                setBackground(UIManager.getColor("ComboBox.background"));
                setForeground(UIManager.getColor("ComboBox.foreground"));
            }

            setText(value.getLabel());

            return this;
        }
    }

    private void invokeFileSelector(JTextField path) {
        File f = new File(path.getText());

        while (f != null && f.toPath() != null && f.toPath().getNameCount() > 0 && !f.exists()) {
            f = f.getParentFile();
        }

        JFileChooser fc = new JFileChooser();

        if (f != null) {
            fc.setSelectedFile(f);
        }
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            path.setText(file.getAbsolutePath());
        }
    }
}
