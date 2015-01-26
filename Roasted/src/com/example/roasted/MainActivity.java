package com.example.roasted;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	DBAdapter db;
	Spinner spinning;
	EditText shopName;
	Context mContext;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//create the database adapter
		db =  new DBAdapter(this);
		loadSpinner();
		
		//copying database over
		
		//database code to create or read DB
		try
		{
			String destPath = "/data/data/" + getPackageName() +"/data";
			
			File f = new File(destPath);
			
			if (!f.exists()) 
			{                   
                f.mkdirs();
                f.createNewFile();
                
                CopyDB(getBaseContext().getAssets().open("mydb"),
                        new FileOutputStream(destPath + "/MyDB"));
			}
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public void CopyDB(InputStream inputStream, OutputStream outputStream) throws IOException 
	{
        //---copy 1K bytes at a time---
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        outputStream.close();
    }
	
	public void addClick(View v)
	{
		shopName = (EditText)findViewById(R.id.editNewShop);
		
		//updated so that it doesn't accept null values 
		String shopped = shopName.toString();
		
		if(shopped == "")
		{
			Toast.makeText(getApplicationContext(), "Must enter a store name.", Toast.LENGTH_SHORT).show();
		}
		else
		{
			loadDialog(shopName);
		}	
		
		//shop name is now the variable that must be sent to Dialog which
		//will then send it to the database.
		
	}
	//delete method
	public void deleteClick(View v)
	{
		//here add onSpinner listener
		//compare listener with database name to delete
		String del = spinning.getSelectedItem().toString();
		
		db.open();
		
		//cursor uses the selected name from spinner to search the database for the rowid of given name 
		Cursor curse = db.getRowID(del);
		
		if(curse.moveToFirst())
		{
		int id = Integer.parseInt(curse.getString(0));
		
		//once rowid is found the contact is deleted from the database
		if(db.deleteContact(id))
		{
			Toast.makeText(getApplicationContext(), "Delete Successful", Toast.LENGTH_SHORT).show();
		}
		else
		{
			Toast.makeText(getApplicationContext(), "Delete Unsuccessful", Toast.LENGTH_SHORT).show();
		}
		}
		else
		{
			Toast.makeText(getApplicationContext(), "Delete Unsuccessful", Toast.LENGTH_SHORT).show();
		}
		
		//on end of delete re-load updated spinner
		loadSpinner();
		
		db.close();
		
	}
	
	
	//method to call Alert Dialog with mulitple EditText fields
	
	//calls new text_entry.xml file with textedit and edittext values in it.
	public void loadDialog(EditText input)
	{
		//basis of code for multiple editTexts in AlertDialog
		//reference: http://newtoknow.blogspot.com/2011/08/android-alert-dialog-with-multi-edit.html
		//by Sathish Kumar C
		
		LayoutInflater factory = LayoutInflater.from(this);

	    final View textEntryView = factory.inflate(R.layout.text_entry, null);
	     //text_entry is a Layout XML file containing two text field to display in alert dialog

	    final EditText address = (EditText) textEntryView.findViewById(R.id.editAddress);
	    final EditText phone = (EditText) textEntryView.findViewById(R.id.editPhone);
	    final EditText email = (EditText) textEntryView.findViewById(R.id.editEmail);
	    
	                
	    address.setText("");
	    phone.setText("");
	    email.setText("");
	    
	    //building alert dialog
	    final AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    alert.setIcon(R.drawable.ic_launcher).setTitle(
	      "Enter the Text:").setView(
	      textEntryView).setPositiveButton("Save",
	      new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog,
	         int whichButton) {
	        
	    	//use of log to check that the correct values are sent through
	        Log.i("AlertDialog","TextEntry 1 Entered "+address.getText().toString());
	        Log.i("AlertDialog","TextEntry 2 Entered "+phone.getText().toString());
	        Log.i("AlertDialog","TextEntry 3 Entered "+email.getText().toString());
	        /* User clicked OK so do some stuff */
	        //to save information to database here
	        
	      //pull what was entered in the editText to be sent to the database on the Dialog box onClick
	        String shop = shopName.getText().toString();
	
			String post = address.getText().toString();
	
			String tel = phone.getText().toString();
	
			String ePos = email.getText().toString();
	        
			//oncreate attaches to the database and query on demand with DBAdapter methods
			
			db.open();
	
			//insert of above fields into the db
			db.insertShop(shop, post, tel, ePos);
			
			shopName.setText("");
			loadSpinner();
	
			db.close();
	       }
	      });
	    alert.show();
		
	}
	
	//method to load the spinner on demand
	public void loadSpinner()
	{
		//the spinner is to be loaded onCreate and to have the items in the spinner at start
		//and then once again on add buttons alertdialog 'ok' click.
		
		spinning = (Spinner)findViewById(R.id.spinShops);
		
		ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinning.setAdapter(adapter);
		
		db.open();
		try
		{
			//on the database call i must also receive the shopid key field
			//which is added to the call in the dbadapter class
			Cursor cursor = db.getShopNames();
			
			
			int shopNameIndex = cursor.getColumnIndexOrThrow("name");
			
			if (cursor.moveToFirst())
	        {
	            do 
	            {
	                adapter.add(cursor.getString(shopNameIndex));
	            } while (cursor.moveToNext());
	        }
			
        }
		finally
        {
			db.close();
        }
	}
	
	public void addSpin(Cursor c)
	{
		List<String> spinnerArray =  new ArrayList<String>();
	    spinnerArray.add(c.getString(0));

	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    Spinner sItems = (Spinner) findViewById(R.id.spinShops);
	    sItems.setAdapter(adapter);
	}
		
	//on click of 'rate coffee' button dialog pops up asking you to rate the coffee
	public void coffeeClick(View v)
	{
		String updateCof = spinning.getSelectedItem().toString();
		
		final AlertDialog.Builder coffeeDialog = new AlertDialog.Builder(this);
		final RatingBar rating = new RatingBar(this);
		rating.setMax(4);
		
		coffeeDialog.setIcon(R.drawable.ic_launcher);
		coffeeDialog.setTitle("Rate " + updateCof+"'s Coffee");
		coffeeDialog.setView(rating);
		
		coffeeDialog.setPositiveButton("Save",
	      new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog,
	         int whichButton) {
	    	
	    	//get the rating to be sent to the update    
	    	float rates = rating.getRating();   
	    	
	    	//get the spin selected item to do database compare
	    	String updateCof = spinning.getSelectedItem().toString();
	        
	      //pull what was entered in the rating to be sent to the database on the Dialog box onClick
	        
			
	    	db.open();
			
			Cursor curse = db.getRowID(updateCof);
			//find the correct position in the database and update it
			if(curse.moveToFirst())
			{
				int id = Integer.parseInt(curse.getString(0));
				
				if(db.updateCoffee(id, rates))
				{
					Toast.makeText(getApplicationContext(), "Update Successful", Toast.LENGTH_SHORT).show();
					
				}
				else
				{
					Toast.makeText(getApplicationContext(), "Update Unsuccessful", Toast.LENGTH_SHORT).show();
				}
			}
			
			db.close();
	       }
	      });
		
		coffeeDialog.show();
		
	}
	
	//foodUpdate method will be exactly the same as coffeeUpdate method but with different names
	public void foodClick(View v)
	{
		String updateFood = spinning.getSelectedItem().toString();
		
		final AlertDialog.Builder foodDialog = new AlertDialog.Builder(this);
		final RatingBar rating = new RatingBar(this);
		rating.setMax(5);
		
		foodDialog.setIcon(R.drawable.ic_launcher);
		foodDialog.setTitle("Rate " + updateFood+"'s Coffee");
		foodDialog.setView(rating);
		
		foodDialog.setPositiveButton("Save",
	      new DialogInterface.OnClickListener() {
	       public void onClick(DialogInterface dialog,
	         int whichButton) {
	    	
	    	//get the rating to be sent to the update    
	    	float rates = rating.getRating();   
	    	
	    	//get the spin selected item to do database compare
	    	String updateFood = spinning.getSelectedItem().toString();
	        
	      //pull what was entered in the rating to be sent to the database on the Dialog box onClick
	        
			
	    	db.open();
			
			Cursor curse = db.getRowID(updateFood);
			
			if(curse.moveToFirst())
			{
				int id = Integer.parseInt(curse.getString(0));
				
				if(db.updateFood(id, rates))
				{
					Toast.makeText(getApplicationContext(), "Update Successful", Toast.LENGTH_SHORT).show();
					
				}
				else
				{
					Toast.makeText(getApplicationContext(), "Update Unsuccessful", Toast.LENGTH_SHORT).show();
				}
			}
			
			db.close();
	       }
	      });
		
		foodDialog.show();
		
	}

	//method to view a single shop and its details
	public void viewShopClick(View v)
	{
		String shopName = spinning.getSelectedItem().toString();
		
        db.open();
        
        Cursor curse = db.getRowID(shopName);
        Cursor c;
        
        if(curse.moveToFirst())
		{
			int id = Integer.parseInt(curse.getString(0));
			c = db.getContact(id);
			
			if (c.moveToFirst())
	        {
	            DisplayShop(c);
	        }
			else
	        {
	            //editOutput.setText("No contact found");
	        }
		}
        db.close();
    }
	
	//method to view all the shops and their ratings
	public void viewAllClick(View v)
	{
		 db.open();
		 
	     Cursor c = db.getAllContacts();
	     
	     String newStr = "";
	     
	     if (c.moveToFirst())
	     {
	         do
	         {
	            newStr += appendDetails(c) + "\n";
	         } while (c.moveToNext());
	         
	         DisplayAllShops(newStr);
	     }
	     
	     db.close();
		
	}
	
	//method called from viewShopClick to have the dialog show with correct formatting
	public void DisplayShop(Cursor c)
    {
		//in here create the textview that shows all the shop details
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    
		String shopName = spinning.getSelectedItem().toString();
		
        alert.setTitle(shopName + " Details");
        
        final TextView shopDetails = new TextView (this);
        shopDetails.setMovementMethod(new ScrollingMovementMethod());
        
        shopDetails.append("Name: " + c.getString(1) + "\n" +
                          "Address:  " + c.getString(2) + "\n"+
                          "Phone:  " + c.getString(3) + "\n"+
                          "Coffee Rating:  " + c.getString(4) + "\n"+
                          "Food Rating:  " + c.getString(5) + "\n");
        
        alert.setView(shopDetails);
    	
    	alert.setPositiveButton("Ok", null);
    	
    	alert.show();
    }
	
	//method called from viewAllClick to have the dialog show with correct formatting
	public void DisplayAllShops(String s)
    {
		//in here create the textview that shows all the shop details
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
	    
        alert.setTitle("Coffee Shops");
        
        final TextView shopDetails = new TextView (this);
        shopDetails.setMovementMethod(new ScrollingMovementMethod());
        
        shopDetails.setText(s);
        
        alert.setView(shopDetails);
    	
    	alert.setPositiveButton("Ok", null);
    	
    	alert.show();
    }
	
	//method called from viewAllClick to use correct formatting before being sent to DisplayAllShops()
	public String appendDetails(Cursor c)
	{
		String deets = "";
		
		deets = "Name: " + c.getString(0) + "\n" +
                "Coffee Rating:  " + c.getString(1) + "\n"+
                "Food Rating:  " + c.getString(2) + "\n";
		
		return deets;
	}
	
	
//last curly
}
