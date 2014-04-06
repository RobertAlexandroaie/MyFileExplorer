package com.fii.myfileexplorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.TreeSet;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.fii.myfileexplorer.model.Item;
import com.fii.myfileexplorer.model.adapter.FileArrayAdapter;

public class MainActivity extends ListActivity {
    private File currentDir;
    private FileArrayAdapter adapter;
    private File fileClipboard = null;
    private int pos;
    private boolean showFileTypes;
    private boolean showPictures = true;
    private boolean showMusic = true;
    private boolean showOthers = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	if (savedInstanceState != null) {
	    currentDir = new File(savedInstanceState.getString("currentDirAbsolutePath"));
	} else {
	    currentDir = new File(getResources().getString(R.string.sd_card_path));
	}

	SharedPreferences sharedPrefs = getSharedPreferences("filter_prefs", 0);
	if (sharedPrefs != null) {
	    currentDir = new File(sharedPrefs.getString("currentDir", getResources().getString(R.string.sd_card_path)));
	    showFileTypes = sharedPrefs.getBoolean("showFileTypes", true);
	    showPictures = sharedPrefs.getBoolean("showPictures", true);
	    showMusic = sharedPrefs.getBoolean("showMusic", true);
	    showOthers = sharedPrefs.getBoolean("showOthers", true);
	}

	fillDir(currentDir);
    }

    private void fillDir(File currentDir) {
	this.setTitle("Current Dir: " + currentDir.getName());
	ArrayList<File> files = new ArrayList<File>(Arrays.asList(currentDir.listFiles()));

	ArrayList<Item> directories = new ArrayList<Item>();
	TreeSet<Item> fileItems = new TreeSet<Item>();

	for (File file : files) {
	    Date lastModifiedDate = new Date(file.lastModified());
	    DateFormat dateFormater = DateFormat.getDateTimeInstance();
	    String lastModified = dateFormater.format(lastModifiedDate);
	    if (file.isDirectory()) {
		File[] fileBuffer = file.listFiles();
		int buffer = 0;
		if (fileBuffer != null) {
		    buffer = fileBuffer.length;
		} else {
		    buffer = 0;
		}
		String itemCount = String.valueOf(buffer);
		if (buffer == 0) {
		    itemCount = itemCount + " item";
		} else {
		    itemCount = itemCount + " items";
		}

		directories.add(new Item(file.getName(), itemCount, lastModified, file.getAbsolutePath(), "directory_icon"));

	    } else if (showOthers) {
		String iconType;
		iconType = "file_icon";

		String name = "";
		if (!showFileTypes) {
		    StringTokenizer stringTokenizer = new StringTokenizer(file.getName(), ".");
		    ArrayList<String> tokens = new ArrayList<String>();

		    while (stringTokenizer.hasMoreTokens()) {
			tokens.add(stringTokenizer.nextToken());
		    }

		    for (int i = 0; i < tokens.size() - 1; i++) {
			name += tokens.get(i);
		    }
		} else {
		    name = file.getName();
		}

		if (!((isPicture(file) && !showPictures) || (isMusic(file) && !showMusic))) {
		    fileItems.add(new Item(name, file.length() + " Byte", lastModified, file.getAbsolutePath(), iconType));
		}
	    }
	}
	Collections.sort(directories);
	directories.addAll(fileItems);

	if (!currentDir.getName().equalsIgnoreCase("sdcard")) {
	    directories.add(0, new Item("..", "Parent Directory", "", currentDir.getParent(), "directory_up"));
	}

	adapter = new FileArrayAdapter(this, R.layout.item_layout, directories);
	this.setListAdapter(adapter);
	ListView listView = this.getListView();
	for (int i = 0; i < listView.getChildCount(); i++) {
	    View view = listView.getChildAt(i);
	    this.registerForContextMenu(view);
	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.filetypes:
	    if (item.isChecked()) {
		showFileTypes = true;
		item.setChecked(false);
	    } else {
		showFileTypes = false;
		item.setChecked(true);
	    }
	    fillDir(currentDir);
	    return true;
	case R.id.show_pictures:
	    if (item.isChecked()) {
		showPictures = false;
		item.setChecked(false);
	    } else {
		showPictures = true;
		item.setChecked(true);
	    }
	    fillDir(currentDir);
	    return true;
	case R.id.show_music:
	    if (item.isChecked()) {
		showPictures = false;
		item.setChecked(false);
	    } else {
		showPictures = true;
		item.setChecked(true);
	    }
	    fillDir(currentDir);
	    return true;
	case R.id.show_other:
	    if (item.isChecked()) {
		showOthers = false;
		item.setChecked(false);
	    } else {
		showOthers = true;
		item.setChecked(true);
	    }
	    fillDir(currentDir);
	    return true;
	default:
	    return super.onOptionsItemSelected(item);
	}
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	// TODO Auto-generated method stub
	super.onCreateContextMenu(menu, v, menuInfo);
	getMenuInflater().inflate(R.menu.contextual_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
	Item fileItem = adapter.getItem(pos);
	File file = new File(fileItem.getPath());
	final File finalFile = file;
	AlertDialog.Builder builder = null;
	AlertDialog alertDialog = null;

	switch (item.getItemId()) {
	case R.id.open:
	    if (fileItem.getImage().equalsIgnoreCase("directory_icon") || fileItem.getImage().equalsIgnoreCase("directory_up")) {
		currentDir = new File(fileItem.getPath());
		fillDir(currentDir);
	    } else if (isPicture(file)) {
		builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.image_preview, null));

		alertDialog = builder.create();
		alertDialog.show();

		Bitmap imagePreview = BitmapFactory.decodeFile(fileItem.getPath());
		ImageView imageView = (ImageView) alertDialog.findViewById(R.id.image_view);
		if (imageView != null) {
		    imageView.setImageBitmap(imagePreview);
		}
	    } else if (file.getName().toLowerCase().contains(".cnt")) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("application/cnt");
		intent.putExtra("filePath", file.getAbsolutePath());
		startActivityForResult(intent, 3453);
	    }
	    return true;
	case R.id.delete:
	    builder = new AlertDialog.Builder(this);
	    builder.setTitle("Delete " + file.getName());
	    builder.setMessage("Are you sure you want to delete " + file.getName() + "?");
	    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
		    delete(finalFile);
		    Toast toast = Toast.makeText(MainActivity.this, "The file " + finalFile.getName() + "has been deleted.", Toast.LENGTH_SHORT);
		    toast.show();

		    fillDir(currentDir);
		}
	    });
	    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
		}
	    });
	    alertDialog = builder.create();
	    alertDialog.show();
	    return true;
	case R.id.copy:
	    fileClipboard = file;

	    Toast toast = Toast.makeText(MainActivity.this, "The file " + finalFile.getName() + "has been copied to clipboard.", Toast.LENGTH_SHORT);
	    toast.show();

	    return true;
	case R.id.paste:
	    if (fileClipboard != null) {
		String path;
		if (file.isDirectory()) {
		    path = file.getAbsolutePath() + "/";
		} else {
		    path = file.getParent() + "/";
		}
		File newFile = new File(path + fileClipboard.getName());
		copy(fileClipboard, newFile);
		fillDir(currentDir);
	    }
	    return true;
	case R.id.rename:
	    builder = new AlertDialog.Builder(this);
	    LayoutInflater inflater = this.getLayoutInflater();
	    final View view = inflater.inflate(R.layout.alert_layout, null);
	    builder.setView(view);
	    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
		    EditText editText = (EditText) view.findViewById(R.id.rename_et);
		    if (editText != null) {
			File newFile = new File(finalFile.getParent(), finalFile.getName());
			try {
			    newFile.createNewFile();
			    if (copy(finalFile, newFile)) {
				finalFile.delete();
			    }
			} catch (IOException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}
		    }
		    fillDir(currentDir);
		}
	    });
	    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
		}
	    });
	    alertDialog = builder.create();
	    EditText editText = (EditText) view.findViewById(R.id.rename_et);
	    if (editText != null) {
		editText.setText(file.getName());
	    }
	    alertDialog.show();
	    return true;
	default:
	    return super.onContextItemSelected(item);
	}
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
	super.onListItemClick(listView, view, position, id);
	Item item = adapter.getItem(position);
	if (item.getImage().equals("directory_up")) {
	    currentDir = new File(item.getPath());
	    fillDir(currentDir);
	} else {
	    this.registerForContextMenu(view);
	    view.showContextMenu();
	    pos = position;
	}
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
	// TODO Auto-generated method stub
	super.onSaveInstanceState(outState);
	outState.putString("currentDirAbsolutePath", currentDir.getAbsolutePath());
    }

    private void delete(File file) {
	if (file != null) {
	    if (!file.isDirectory()) {
		file.delete();
	    } else {
		for (File fileInDir : Arrays.asList(file.listFiles())) {
		    delete(fileInDir);
		}
	    }
	}
    }

    private boolean copy(File src, File dst) {
	InputStream in;
	OutputStream out;
	try {
	    in = new FileInputStream(src);
	    out = new FileOutputStream(dst);

	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
		out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	    return true;
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return false;
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return false;
	}
    }

    private boolean isPicture(File file) {
	String name = file.getName().toLowerCase();
	if (name.contains(".png") || name.contains(".jpg") || name.contains(".jpeg") || name.contains(".bmp")) {
	    return true;
	} else {
	    return false;
	}
    }

    private boolean isMusic(File file) {
	String name = file.getName().toLowerCase();
	if (name.contains(".mp3") || name.contains(".flac") || name.contains(".wav") || name.contains(".aac")) {
	    return true;
	} else {
	    return false;
	}
    }

    private void saveSharedPrefs() {
	SharedPreferences sharedPrefs = getSharedPreferences("filter_prefs", 0);
	SharedPreferences.Editor edit = sharedPrefs.edit();
	edit.putBoolean("showPictures", showPictures);
	edit.putBoolean("showMusic", showMusic);
	edit.putBoolean("showOthers", showOthers);
	edit.putBoolean("showFileTypes", showFileTypes);
	edit.putString("currentFile", currentDir.getAbsolutePath());
	edit.commit();
    }

    @Override
    protected void onStop() {
	super.onStop();

	saveSharedPrefs();
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();

	saveSharedPrefs();
    }

    @Override
    protected void onPause() {
	super.onPause();

	saveSharedPrefs();
    }
}
