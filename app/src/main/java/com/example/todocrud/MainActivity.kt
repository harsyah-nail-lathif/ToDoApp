package com.example.todocrud

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //untuk menggunakan fingsu fungsi yang ada di dalam kelas DatabaseReference
    private lateinit var databaseRef:  DatabaseReference
    //variabel cekData berguna untuk membaca data yang ada di dalam Database
    private lateinit var cekData: DatabaseReference
    //di gunakan untuk mengecek aktivitas yang terjadi di dalam database
    private lateinit var readDataListener: ValueEventListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseRef = FirebaseDatabase.getInstance().reference

        btn_tambah.setOnClickListener{
            /*menginisialisasikan data dan men-konvert data
            yang ada di dalam tabel menjadi String*/
            val nama = input_nama.text.toString()
            if (nama.isBlank()){
                toasData("Kolom Tidak Boleh Kosong")
            }else{
                tambahData(nama)
            }
        }

        btn_hapus.setOnClickListener{
            val nama = input_nama.text.toString()

            if (nama.isBlank()){
                toasData("Kolom tidak boleh kosong")
            }else{
                hapusData(nama)
            }
        }

        btn_update.setOnClickListener{
            val kalimatAwal = input_nama.text.toString()
            val kalimatUpdate = edt_nama.text.toString()
            if (kalimatAwal.isBlank()||kalimatUpdate.isBlank()){
                toasData("kolom tidak boleh Kosong")
            }else{
                updateData(kalimatAwal, kalimatUpdate)
            }
        }

        cekDataKalimat()
    }

    private fun updateData(kalimatAwal: String, kalimatUpdate: String) {
        val dataUpdate = HashMap<String, Any>()
        dataUpdate["Nama"] = kalimatUpdate

        val dataListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.childrenCount > 0){
                    databaseRef.child("Daftar Nama")
                        .child(kalimatAwal)
                        .updateChildren(dataUpdate)
                        .addOnCompleteListener{task ->
                            if (task.isSuccessful){
                                toasData("Data berhasi di update")
                            }else{
                                toasData("Data tersebut tidak ada di dalam Database")
                            }
                        }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        }
        val dataAsal = databaseRef.child("Daftar Nama")
            .child(kalimatAwal)
        dataAsal.addListenerForSingleValueEvent(dataListener)
    }

    private fun hapusData(nama: String) {
        val dataListener = object : ValueEventListener{
            //onDataChange berfungsi untuk mengawasi aktifitas seperti pemabahan dan penghapusan data
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount > 0){
                    //jika kolom di temukan, maka data yang ada di dalam koloma akan di hapus
                    databaseRef.child("Daftar Nama").child(nama)
                        .removeValue()
                        .addOnCompleteListener{task ->
                            if (task.isSuccessful) toasData("$nama berhasi di hapus")
                        }
                }else{
                    toasData("tidak ada data yang bisa d hapus")
                }
            }
            override fun onCancelled(p0: DatabaseError){
                toasData("Batal menghapus data")
            }
        }
        //fungsi pengecekan data pada Database sebelum melakukan penghapusan
        val cekData = databaseRef.child("Daftar Nama")
            .child(nama)
        //addValueListener = unlimited loop
        //addListenerForSingleValueEvent = looped once
        cekData.addListenerForSingleValueEvent(dataListener)
    }

    private fun cekDataKalimat() {
        readDataListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //cek di dalam database apakah di sana sudah terdapat data atau belum
                if (snapshot.childrenCount > 0){
                    var textData = " "
                    for (data in snapshot.children){
                        val nilai = data.getValue(ModelName::class.java) as ModelName
                        textData += "${nilai.Nama}\n"
                    }
                    txt_nama.text = textData
                }
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        }
        //cek data yang ada di dalam tabel Daftar nama
        cekData = databaseRef.child("Daftar Nama")
        //di gunakan untuk memantau apa saja yang ada perubahan di dalam Database
        cekData.addValueEventListener(readDataListener)
    }

    private fun tambahData(nama: String) {
        val data = HashMap<String, Any>()
        data["Nama"] = nama

        //logika penambahan Data pada datasbase dan melakukan pengecekan

        val datalistener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //children ini berguna untuk menghitung Data
                //Jika data kurang dari satu maka data harus di tambahkan

                if (snapshot.childrenCount < 1){
                    val tambahData = databaseRef.child("Daftar Nama")
                        .child(nama)
                        .setValue(data)
                    tambahData.addOnCompleteListener{task ->
                        if (task.isSuccessful){
                            toasData("$nama sudah di tambahkan ke dalam Database")
                        }else{
                            toasData("gagal menambahkan $nama ke Database")
                        }
                    }
                }else{
                    toasData("Data tersebut sudah ada didalam Databaes")
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                toasData("Terjadi error saat menambahkan Data")
            }
        }

        /* melakukan pengecekan pada Database apakah Data yang sedang di masukan sudah ada
        di dalam Databaseatau belum */

        databaseRef.child("Daftar Nama")
            .child(nama).addListenerForSingleValueEvent(datalistener)
    }

    private fun toasData(pesan: String){
        Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()
    }
}