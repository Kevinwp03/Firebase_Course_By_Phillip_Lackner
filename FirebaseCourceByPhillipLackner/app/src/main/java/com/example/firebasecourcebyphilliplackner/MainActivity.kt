package com.example.firebasecourcebyphilliplackner

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val personCollectionRef = Firebase.firestore.collection("persons")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSendToDatabase.setOnClickListener {
            val firstName = etFirstName.text.toString()
            val lastName = etLastName.text.toString()
            val age = etAge.text.toString().toInt()
            val person = Person(firstName, lastName, age)
            savePerson(person)
        }
    }

    private fun savePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        try {
            personCollectionRef.add(person).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Successfully save data", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}