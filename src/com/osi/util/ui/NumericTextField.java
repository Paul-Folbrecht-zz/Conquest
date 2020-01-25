package com.osi.util.ui;


import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;


/**
 * This class is a JTextField replacement that permits only numeric characters.
 *
 * @author Paul Folbrecht
 */
public class NumericTextField extends JTextField {
    public NumericTextField(int cols) {
        super(cols);
    }

    protected Document createDefaultModel() {
        return new NumericOnlyDocument();
    }

    protected static class NumericOnlyDocument extends PlainDocument {
        public void insertString(int offset, String str, AttributeSet a)
                throws BadLocationException {

            if (str == null) {
                return;
            }

            for (int index = 0; index < str.length(); index++) {
                if (Character.isDigit(str.charAt(index)) == false) {
                    return;
                }
            }

            super.insertString(offset, str, a);
        }
    }
}
