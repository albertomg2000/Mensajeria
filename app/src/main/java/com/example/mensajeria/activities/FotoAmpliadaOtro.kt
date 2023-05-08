package com.example.mensajeria.activities



import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import com.example.mensajeria.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.ByteArrayOutputStream

class FotoAmpliadaOtro : AppCompatActivity() {
    private var chatId = ""
    private var otherUser = ""
    private lateinit var imageView: ImageView
    private var user = ""
    var MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0
    val IMAGE_REQUEST_CODE = 1_000;
    var FotoCambiada = false

    private lateinit var LoadingImage: ImageView
    private lateinit var LoadingLetter: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.foto_ampliada)
        intent.getStringExtra("user")?.let { user = it }
        intent.getStringExtra("chatId")?.let { chatId = it }
        intent.getStringExtra("otherUser")?.let { otherUser = it }
        // Configurar el toolbar
        val toolbar = findViewById<Toolbar>(R.id.custom_toolbar)
        val botonPerfil = findViewById<Button>(R.id.buttonPerfil)
        val titulo = findViewById<TextView>(R.id.textView2)
        setSupportActionBar(toolbar)
        toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black))
        actionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.negro)))
        window.statusBarColor = ContextCompat.getColor(this, R.color.negro)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = ""
        val backButton = findViewById<ImageButton>(R.id.back_button)

        botonPerfil.isInvisible=true




       backButton.setOnClickListener {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("chatId", chatId)
        intent.putExtra("user", user)
        intent.putExtra("otherUser", otherUser)
        startActivity(intent)
        true
        finish()}
        val fullText = otherUser
        val atIndex = fullText.lastIndexOf("@")
        if (atIndex != -1) {
            val username = fullText.substring(0, atIndex)
            titulo.text = username
        }
        titulo.textSize = 20f // 20sp

        val params = titulo.layoutParams as ConstraintLayout.LayoutParams
        params.marginStart = resources.getDimensionPixelSize(R.dimen.toolbar_title_margin_start)
        titulo.layoutParams = params


        // Obtener la referencia al ImageView
        imageView = findViewById(R.id.image_view)
        LoadingImage = findViewById(R.id.loading_image_view)
        LoadingLetter = findViewById(R.id.loading_letter_view)
        imageView.visibility = View.GONE
        // Obtener la referencia al archivo de imagen en el storage
        val storageRef = FirebaseStorage.getInstance().getReference()
            .child("images/users/" + otherUser + "/profile.png")

        // Descargar la imagen y mostrarla en el ImageView
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            imageView.setImageBitmap(bmp)
            LoadingLetter.visibility = View.GONE
            LoadingImage.visibility = View.GONE
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.visibility = View.VISIBLE
        }.addOnFailureListener { exception ->
            // Manejar errores
        }

    }
    override fun onBackPressed() {

        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("chatId", chatId)
        intent.putExtra("user", user)
        intent.putExtra("otherUser", otherUser)
        startActivity(intent)
        true
        finish()}

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Manejar eventos del toolbar
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    private fun uploadImageToFirebaseStorage(bitmap: Bitmap) {
        // Mostrar diálogo de carga
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Subiendo imagen...")
        progressDialog.show()

        // Convertir el Bitmap a un arreglo de bytes
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        // Subir imagen a Storage
        val storageRef = Firebase.storage.reference.child("images/users/$otherUser/profile.png")
        val uploadTask = storageRef.putBytes(data)

        uploadTask.addOnSuccessListener {
            // Ocultar diálogo de carga
            progressDialog.dismiss()

            storageRef.downloadUrl.addOnSuccessListener { uri ->
                // Aquí puedes hacer algo con la URL de descarga de la imagen, como guardarla en la base de datos.
            }.addOnFailureListener { exception ->
                // Manejar errores de descarga de URL.
            }
        }.addOnFailureListener { exception ->
            // Manejar errores de carga de imagen.

            // Ocultar diálogo de carga
            progressDialog.dismiss()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            resultData?.data?.also { uri ->
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        // Explicar al usuario por qué se necesita el permiso y luego solicitarlo
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                        )
                    }
                } else {
                    val options = BitmapFactory.Options()
                    val bitmap = BitmapFactory.decodeStream(
                        contentResolver.openInputStream(uri),
                        null,
                        options
                    )

                    // Establecer la imagen en el ImageView con el ScaleType correspondiente
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    imageView.setImageBitmap(bitmap)

                    // Obtener el Bitmap de la imagen escalada en el ImageView
                    imageView.isDrawingCacheEnabled = true
                    imageView.buildDrawingCache()
                    val scaledBitmap = imageView.drawingCache

                    // Subir la imagen a Firebase Storage
                    if (scaledBitmap != null) {
                        uploadImageToFirebaseStorage(scaledBitmap)
                    }
                }
            }
        }
    }

}


