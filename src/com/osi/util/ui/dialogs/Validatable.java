package com.osi.util.ui.dialogs;


/**
 * @author Paul Folbrecht
 */
public interface Validatable {
    public void onAdded();

    public boolean validateData();

    public void onOk();

    public void onCancel();
}
