package com.example.aaron.notekeeper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    //create a final in the intended activity
    public static final String NOTE_POSITION ="com.example.aaron.notekeeper.NOTE_POSITION";
    public static final int POSITION_NOT_FOUND = -1;
    private NoteInfo note;
    private boolean isNewNote;
    private Spinner spinnerCourses;
    private EditText textNoteTitle;
    private EditText textNoteText;
    private int notePosition;
    private boolean isCanceling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        spinnerCourses = findViewById(R.id.spinner_courses);

        List<CourseInfo> courses=DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses=
                new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(adapterCourses);

        //get extras from intent
        readDisplayStateValues();

        textNoteTitle = findViewById(R.id.text_note_title);
        textNoteText = findViewById(R.id.text_note_text);

        if(!isNewNote)
            displayNote(spinnerCourses, textNoteTitle, textNoteText);
    }

    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        List<CourseInfo> courses=DataManager.getInstance().getCourses();
        int courseIndex=courses.indexOf(note.getCourse());
        spinnerCourses.setSelection(courseIndex);
        textNoteTitle.setText(note.getTitle());
        textNoteText.setText(note.getText());
    }

    private void readDisplayStateValues() {
        Intent intent=getIntent();
        int position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_FOUND);
        isNewNote = position==POSITION_NOT_FOUND;

        if(isNewNote){
            createNewNote();
        }else {
            note = DataManager.getInstance().getNotes().get(position);
        }
    }

    private void createNewNote() {
        DataManager dm= DataManager.getInstance();
        notePosition = dm.createNewNote();
        note = dm.getNotes().get(notePosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(isCanceling){
            if(isNewNote) {
                DataManager.getInstance().removeNote(notePosition);
            }
        }else{
            saveNote();
        }

    }

    private void saveNote() {
        note.setCourse((CourseInfo) spinnerCourses.getSelectedItem());
        note.setTitle(textNoteTitle.getText().toString());
        note.setText(textNoteText.getText().toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        }else if(id==R.id.action_cancel){
            isCanceling = true;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) spinnerCourses.getSelectedItem();
        String subject=textNoteTitle.getText().toString();
        String text="Check out what i learnt in the Pluralsight course \"" +
                course.getTitle()+"\"\n"+textNoteText.getText().toString();
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("message/2822");
        intent.putExtra(Intent.EXTRA_SUBJECT,subject);
        intent.putExtra(Intent.EXTRA_TEXT,text);

        startActivity(intent);
    }
}
