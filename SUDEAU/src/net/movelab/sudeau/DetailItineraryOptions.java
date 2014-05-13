package net.movelab.sudeau;

import net.movelab.sudeau.model.Route;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


public class DetailItineraryOptions extends DialogFragment {
	
	private static final String TITLE = "Tria la opci� de ruta que prefereixes:";
	private static final String OPTION_1 = "Vull rec�rrer la ruta, sense fer un seguiment de la meva posici�.";
	private static final String OPTION_2 = "Vull rec�rrer la ruta, fent un seguiment en tot moment de la meva posici�.";
	private static final String OPTION_3 = "Vull rec�rrer la ruta, fent un seguiment en tot moment i desar la meva pr�pia versi� de la ruta.";
    
    public Dialog onCreateDialog(Bundle savedInstanceState){
    	
    	CharSequence[] options = new String[]{
			OPTION_1,
			OPTION_2,
			OPTION_3
    	};
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(TITLE)
               .setItems(options, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {                	   
                	   
               }
        });
        return builder.create();
    }
}
