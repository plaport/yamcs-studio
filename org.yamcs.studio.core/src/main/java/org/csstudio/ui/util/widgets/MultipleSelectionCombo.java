/********************************************************************************
 * Copyright (c) 2013, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util.widgets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Combo-type widget that allows selecting multiple items.
 *
 * <p>
 * Takes a list of {@link Object}s as input.
 *
 * <p>
 * The <code>toString()</code> of each Object is displayed in a drop-down list. Overriding the stringRepresention()
 * method, the user can define an alternative way to convert T to String.
 *
 * <p>
 * One or more items can be selected, they're also displayed in the text field.
 *
 * <p>
 * Items can be entered in the text field, comma-separated. If entered text does not match a valid item, text is
 * highlighted and tool-tip indicates error.
 *
 * <p>
 * Keyboard support: 'Down' key in text field opens drop-down. Inside drop-down, single item can be selected via cursor
 * key and 'RETURN' closes the drop-down.
 *
 * TODO Auto-completion while typing?
 */
public class MultipleSelectionCombo<T> extends Composite {
    final private static String SEPARATOR = ", ";
    final private static String SEPERATOR_PATTERN = "\\s*,\\s*";

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private Display display;

    private Text text;

    /** Pushing the drop_down button opens the popup */
    private Button drop_down;

    private Shell popup;
    private org.eclipse.swt.widgets.List list;

    /** Items to show in list */
    private List<T> items = new ArrayList<>();

    /** Selection indices */
    private List<Integer> selectionIndex = new ArrayList<>();

    private String tool_tip = null;
    private Color text_color = null;

    /**
     * When list looses focus, the event time is noted here. This prevents the drop-down button from re-opening the list
     * right away.
     */
    private long lost_focus = 0;

    private volatile boolean modify = false;

    public MultipleSelectionCombo(Composite parent, int style) {
        super(parent, style);
        createComponents(parent);
    }

    private void createComponents(Composite parent) {
        display = parent.getDisplay();
        var layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        setLayout(layout);

        addPropertyChangeListener(e -> {
            switch (e.getPropertyName()) {
            case "selection":
                if (modify) {
                    break;
                } else {
                    updateText();
                    break;
                }
            case "items":
                setSelection(Collections.<T> emptyList());
                break;
            default:
                break;
            }
        });

        text = new Text(this, SWT.BORDER);
        var gd = new GridData(SWT.FILL, 0, true, false);
        text.setLayoutData(gd);
        text.addModifyListener(e -> {
            // Analyze text, update selection
            var items_text = text.getText();
            modify = true;
            setSelection(items_text);
            modify = false;
        });
        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.keyCode) {
                case SWT.ARROW_DOWN:
                    drop(true);
                    return;
                case SWT.ARROW_UP:
                    drop(false);
                    return;
                case SWT.CR:
                    modify = false;
                    updateText();
                }
            }
        });

        drop_down = new Button(this, SWT.ARROW | SWT.DOWN);
        gd = new GridData(SWT.FILL, SWT.FILL, false, false);
        gd.heightHint = text.getBounds().height;
        drop_down.setLayoutData(gd);
        drop_down.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Was list open, user clicked this button to close,
                // and list self-closed because is lost focus?

                // e.time is an unsigned integer and should be AND'ed with
                // 0xFFFFFFFFL so that it can be treated as a signed long.
                if ((e.time & 0xFFFFFFFFL) - lost_focus <= 300) {
                    return; // Done
                }

                // If list is not open, open it
                if (!isDropped()) {
                    drop(true);
                }
            }
        });
    }

    @Override
    public void setForeground(Color color) {
        text_color = color;
        text.setForeground(color);
    }

    @Override
    public void setToolTipText(String tooltip) {
        tool_tip = tooltip;
        text.setToolTipText(tooltip);
        drop_down.setToolTipText(tooltip);
    }

    /**
     * Define items to be displayed in the list, and returned as the current selection when selected.
     */
    public void setItems(List<T> items) {
        var oldValue = this.items;
        this.items = items;
        changeSupport.firePropertyChange("items", oldValue, this.items);
    }

    /**
     * Get the list of items
     *
     * @return list of selectable items
     */
    public List<T> getItems() {
        return this.items;
    }

    /**
     * Set items that should be selected.
     *
     * <p>
     * Selected items must be on the list of items provided via <code>setItems</code>
     */
    public void setSelection(List<T> selection) {
        var oldValue = this.selectionIndex;
        var newSelectionIndex = new ArrayList<Integer>(selection.size());
        for (var t : selection) {
            var index = items.indexOf(t);
            if (index >= 0) {
                newSelectionIndex.add(items.indexOf(t));
            }
        }
        this.selectionIndex = newSelectionIndex;
        changeSupport.firePropertyChange("selection", oldValue, this.selectionIndex);
    }

    /**
     * set the items to be selected, the selection is specified as a string with values separated by
     * MultipleSelectionCombo.SEPARATOR
     *
     * @param selection
     *            Items to select in the list as comma-separated string
     */
    public void setSelection(String selection) {
        setSelection("".equals(selection) ? new String[0] : selection.split(SEPERATOR_PATTERN));
    }

    /**
     * Set the items to be selected
     */
    public void setSelection(String[] selections) {
        var oldValue = this.selectionIndex;
        List<Integer> newSelectionIndex;
        if (selections.length > 0) {
            newSelectionIndex = new ArrayList<>(selections.length);
            // Locate index for each item
            for (var item : selections) {
                int index = getIndex(item);
                if (index >= 0 && index < items.size()) {
                    newSelectionIndex.add(getIndex(item));
                    text.setForeground(text_color);
                    text.setToolTipText(tool_tip);
                } else {
                    text.setForeground(display.getSystemColor(SWT.COLOR_RED));
                    text.setToolTipText("Text contains invalid items");
                }

            }
        } else {
            newSelectionIndex = Collections.emptyList();
        }
        this.selectionIndex = newSelectionIndex;
        changeSupport.firePropertyChange("selection", oldValue, this.selectionIndex);
    }

    /**
     * return the index of the object in items with the string representation _string_
     */
    private Integer getIndex(String string) {
        for (var item : items) {
            if (stringRepresention(item).equals(string)) {
                return items.indexOf(item);
            }
        }
        return -1;
    }

    /**
     * get the list of items currently selected. Note: this does not return the list in the order of selection.
     *
     * @return the list of selected items
     */
    public List<T> getSelection() {
        var selection = new ArrayList<T>(this.selectionIndex.size());
        for (var index : this.selectionIndex) {
            selection.add(items.get(index));
        }
        return Collections.unmodifiableList(selection);
    }

    /** Update <code>selection</code> from <code>list</code> */
    private void updateSelectionFromList() {
        setSelection(list.getSelection());
    }

    /** Update <code>text</code> to reflect <code>selection</code> */
    private void updateText() {
        var buf = new StringBuilder();
        for (var index : selectionIndex) {
            if (buf.length() > 0) {
                buf.append(SEPARATOR);
            }
            buf.append(stringRepresention(items.get(index)));
        }
        text.setText(buf.toString());
        text.setSelection(buf.length());
    }

    /** @return <code>true</code> if drop-down is visible */
    private boolean isDropped() {
        return popup != null;
    }

    /**
     * @param drop
     *            Display drop-down?
     */
    private void drop(boolean drop) {
        if (drop == isDropped()) {
            return;
        }
        if (drop) {
            createPopup();
        } else {
            hidePopup();
        }
    }

    /** Create shell that simulates drop-down */
    private void createPopup() {
        popup = new Shell(getShell(), SWT.NO_TRIM | SWT.ON_TOP);
        popup.setLayout(new FillLayout());
        list = new org.eclipse.swt.widgets.List(popup, SWT.MULTI | SWT.V_SCROLL);
        list.setToolTipText(tool_tip);

        // Position popup under the text box
        var bounds = text.getBounds();
        bounds.y += bounds.height;
        // As high as necessary for items
        bounds.height = 5 + 2 * list.getBorderWidth() + list.getItemHeight() * items.size();
        // ..with limitation
        bounds.height = Math.min(bounds.height, display.getBounds().height / 2);
        // Map to screen coordinates
        bounds = display.map(text, null, bounds);
        popup.setBounds(bounds);
        popup.open();

        // Update text from changed list selection
        list.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateSelectionFromList();
                updateText();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                updateSelectionFromList();
                updateText();
                hidePopup();
            }
        });

        var stringItems = new String[items.size()];
        for (var i = 0; i < items.size(); i++) {
            stringItems[i] = stringRepresention(items.get(i));
        }
        list.setItems(stringItems);
        var intSelectionIndex = new int[selectionIndex.size()];
        for (var i = 0; i < intSelectionIndex.length; i++) {
            intSelectionIndex[i] = selectionIndex.get(i);
        }
        list.setSelection(intSelectionIndex);

        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.CR) {
                    hidePopup();
                }
            }
        });
        // Hide popup when loosing focus
        list.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // This field is an unsigned integer and should be AND'ed with
                // 0xFFFFFFFFL so that it can be treated as a signed long.
                lost_focus = e.time & 0xFFFFFFFFL;
                hidePopup();
            }
        });
        list.setFocus();
    }

    /** Hide popup shell */
    private void hidePopup() {
        if (popup != null) {
            popup.close();
            popup.dispose();
            popup = null;
        }
        text.setFocus();
    }

    /**
     * Register a PropertyChangeListener on this widget. the listener will be notified when the items or the selection
     * is changed.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * remove the PropertyChangeListner
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Override this method to define the how the object should be represented as a string.
     *
     * @param object
     * @return the string representation of the object
     */
    public String stringRepresention(T object) {
        return object.toString();
    }
}
