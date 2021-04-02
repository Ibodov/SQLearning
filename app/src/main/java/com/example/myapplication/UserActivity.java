package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class UserActivity extends AppCompatActivity {

    EditText nameBox;
    EditText yearBox;
    Button delButton;
    Button saveButton;

    DatabaseHelper sqlHelper;
    SQLiteDatabase db;
    Cursor userCursor;
    long userId=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        nameBox = (EditText) findViewById(R.id.name);
        yearBox = (EditText) findViewById(R.id.year);
        delButton = (Button) findViewById(R.id.deleteButton);
        saveButton = (Button) findViewById(R.id.saveButton);

        sqlHelper = new DatabaseHelper(this);
        db = sqlHelper.getWritableDatabase();

//При обновлении или удалении объекта из списка из главной activity в UserActivity будет передаваться id объекта:
// long userId=0;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getLong("id");
        }
//Если из MainActivity не было передано id, то устанавливаем его значение 0, следовательно, у нас будет добавление, а не редактирование/удаление
        // если 0, то добавление
//Если id определен, то получаем по нему из базы данных объект для редактирования/удаления:
        if (userId > 0) {
            // получаем элемент по id из бд
            userCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE + " where " +
                    DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
            userCursor.moveToFirst();
            nameBox.setText(userCursor.getString(1));
            yearBox.setText(String.valueOf(userCursor.getInt(2)));
            userCursor.close();
        } else {
            // скрываем кнопку удаления
            delButton.setVisibility(View.GONE);
        }
    }

    public void save(View view){
//Для добавления или обновления нам надо создать объект ContentValues. Данный объект представляет словарь, который содержит
// набор пар "ключ-значение". Для добавления в этот словарь нового объекта применяется метод put. Первый параметр
// метода - это ключ, а второй - значение, например:

//В данном же случае добавляются введенные в текстовое поля значения:
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_NAME, nameBox.getText().toString());
        cv.put(DatabaseHelper.COLUMN_YEAR, Integer.parseInt(yearBox.getText().toString()));

        if (userId > 0) {
//При обновлении в метод update() передается название таблицы, объект ContentValues
// и критерий, по которому происходит обновление (в данном случае столбец id):
            db.update(DatabaseHelper.TABLE, cv, DatabaseHelper.COLUMN_ID + "=" + String.valueOf(userId), null);
        } else {
//Метод insert() принимает название таблицы, объект ContentValues с добавляемыми значениями.
// Второй параметр является необязательным: он передает столбец, в который надо добавить значение NULL:
            db.insert(DatabaseHelper.TABLE, null, cv);
        }
//Вместо этих методов, как в прошлой теме, можно использовать метод execSQL() с точным указанием выполняемого sql-выражения.
// В то же время методы delete/insert/update имеют преимущество - они возвращают id измененной записи,
// по которому мы можем узнать об успешности операции, или -1 в случае неудачной операции:
 /*       long result = db.insert(DatabaseHelper.TABLE, null, cv);
        if(result>0){

            // действия
        }
*/
        //После каждой операции выполняется метод goHome(), который возвращает на главную activity.
        goHome();
    }

//Для выполнения операций по вставке, обновлению и удалению данных SQLiteDatabase имеет методы insert(), update() и delete().
// Эти методы вызываются в обработчиках кнопок:
    public void delete(View view){
        db.delete(DatabaseHelper.TABLE, "_id = ?", new String[]{String.valueOf(userId)});
        goHome();
    }
// В метод delete() передается название таблицы, а также столбец, по которому происходит удаление, и его значение. В качестве критерия можно
// выбрать несколько столбцов, поэтому третьим параметром идет массив. Знак вопроса ? обозначает параметр, вместо которого подставляется
// значение из третьего параметра.


    private void goHome(){
        // закрываем подключение
        db.close();
        // переход к главной activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}