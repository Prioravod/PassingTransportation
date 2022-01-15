package ru.petrovpavel.passingtransportation.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.text.MessageFormat;

public class RouteConfirmationDialogBuilder {

    public static final String CONFIRM_TITLE = "Подтвержение";
    public static final String CONFIRM_TEMPLATE = "Задать маршрут?\n{0} -> {1}";
    public static final String APPROVE_BUTTON = "Да";
    public static final String REJECT_BUTTON = "Нет";

    private RouteConfirmationDialog routeConfirmationDialog;

    public static void build(Context context, String originAlias, String destinationAlias) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(CONFIRM_TITLE);
        builder.setMessage(fillInMessageTemplate(originAlias, destinationAlias));

        builder.setPositiveButton(APPROVE_BUTTON, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog

                dialog.dismiss();
            }
        });

        builder.setNegativeButton(REJECT_BUTTON, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private static String fillInMessageTemplate(String originAlias, String destinationAlias) {
        return MessageFormat.format(CONFIRM_TEMPLATE, originAlias, destinationAlias);
    }

}

