package com.example.firebasecourcebyphilliplackner

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
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
            val person = getOldPerson()
            savePerson(person)
        }

        btnRetreiveData.setOnClickListener {
            retrievePersons()
        }

        btnUpdateData.setOnClickListener {
            val oldPerson = getOldPerson()
            val newPerson = getNewPersonMap()
            updatePerson(oldPerson, newPerson)
        }

        btnDeletePerson.setOnClickListener {
            val person = getOldPerson()
            deletePerson(person)
        }

        btnDoBatchWrite.setOnClickListener {
            changeName("Hve8diBwJ5v7YMaKX4c3", "Elon", "Musk")
        }

        btnTransaction.setOnClickListener {
            birthDay("Hve8diBwJ5v7YMaKX4c3")
        }
    }

    //Menghapus data
    private fun deletePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = personCollectionRef
                .whereEqualTo("firstName", person.firstName)
                .whereEqualTo("lastName", person.lastName)
                .whereEqualTo("age", person.age)
                .get()
                .await()
        if (personQuery.documents.isNotEmpty()) {
            for (document in personQuery) {
                try {
                    personCollectionRef.document(document.id).delete().await() // ini akan mahapus semua nilai pada data yang dipilih
                    /*personCollectionRef.document(document.id).update(mapOf( // ini akan menghapus salah satu nilai pada data
                            "firstName" to FieldValue.delete()
                    )).await()*/
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message,
                                Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "No person matched the query",
                        Toast.LENGTH_LONG).show()
            }
        }
    }

    // function untuk mengupdate(mengubah) data
    private fun updatePerson(person: Person, newPersonMap: Map<String, Any>) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = personCollectionRef
                .whereEqualTo("firstName", person.firstName)
                .whereEqualTo("lastName", person.lastName)
                .whereEqualTo("age", person.age)
                .get()
                .await()
        if (personQuery.documents.isNotEmpty()) {
            for (document in personQuery) {
                try {
                    personCollectionRef.document(document.id).update("firstName", person.firstName) // fungsinya apa?
                    personCollectionRef.document(document.id).set(
                            newPersonMap,
                            SetOptions.merge() //
                    ).await()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message,
                                Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "No person matched the query",
                        Toast.LENGTH_LONG).show()
            }
        }
    }

    //mengambil data lama
    private fun getOldPerson(): Person {
        val firstName = etFirstName.text.toString()
        val lastName = etLastName.text.toString()
        val age = etAge.text.toString().toInt()
        return Person(firstName, lastName, age)
    }

    //mengirim data baru dan memilih lokasi update datanya
    private fun getNewPersonMap(): Map<String, Any> {
        val firstName = etUpdateFirstName.text.toString()
        val lastName = etUpdateLastname.text.toString()
        val age = etUpdateAge.text.toString() //jangan di convert ke int dulu, karena akan crash
        val map = mutableMapOf<String, Any>()
        if (firstName.isNotEmpty()) {
            map["firstName"] = firstName
        }
        if (lastName.isNotEmpty()) {
            map["lastName"] = lastName
        }
        if (age.isNotEmpty()) {
            map["age"] = age.toInt()
        }
        return map
    }

    // Do Transaction
    private fun birthDay(personId: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Firebase.firestore.runTransaction{ transaction ->
                val personRef = personCollectionRef.document(personId)
                val person = transaction.get(personRef)  // document snapshot?
                val newAge = person["age"] as Long + 1
                transaction.update(personRef, "age", newAge)
                null
            }.await()

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

        //Batch Write, -> mengganti data sesuai lokasi code folder yang di set
        private fun changeName(
                personId: String,
                newFirstname: String,
                newLastname: String,
        ) = CoroutineScope(Dispatchers.IO).launch {
            try {
                Firebase.firestore.runBatch { batch ->
                    val personRef = personCollectionRef.document(personId)
                    batch.update(personRef, "firstName", newFirstname)
                    batch.update(personRef, "lastName", newLastname)
                }.await()
            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        private fun subscribeToRealtimeUpdate() {
            personCollectionRef.addSnapshotListener { querySnapshot, firebaseFirestoreExeption ->
                firebaseFirestoreExeption?.let {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                querySnapshot?.let {
                    val sb = StringBuilder()
                    for (document in querySnapshot.documents) {
                        val person = document.toObject<Person>() //(Person::class.java)
                        sb.append("$person\n") //append = menambahkan
                    }
                    tvPersons.text = sb.toString()
                }
            }
        }

        //mengambil data
        private fun retrievePersons() = CoroutineScope(Dispatchers.IO).launch {
            val fromAge = etFrom.text.toString().toInt()
            val toAge = etTo.text.toString().toInt()
            try {
                //data class harus diisi default value
                val querySnapshot = personCollectionRef
                        .whereGreaterThan("age", fromAge) // lebih besar dari
                        .whereLessThan("age", toAge)  // Lebih kecil dari
                        .orderBy("age")
                        .get()
                        .await() //buat querynya
                val sb = StringBuilder()
                for (document in querySnapshot.documents) {
                    val person = document.toObject<Person>() //(Person::class.java)
                    sb.append("$person\n") //append = menambahkan
                }
                withContext(Dispatchers.Main) {
                    tvPersons.text = sb.toString()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        //Menyimpan datanya
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