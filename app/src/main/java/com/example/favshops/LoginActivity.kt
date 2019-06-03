package com.example.favshops

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.info_verify_dialog.view.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        signInButton.setOnClickListener(this)
        createAccountButton.setOnClickListener(this)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
    }

    public override fun onStart() {
        super.onStart()
    }

    private fun createAccount(email: String, password: String) {
        if (!validateForm()) {
            return
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    database.child("users").child("username").setValue(user?.email)
                    updateUI(user)
                } else {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this@LoginActivity);
                    val inflater: LayoutInflater = LayoutInflater.from(this@LoginActivity)

                    val v: View = inflater.inflate(R.layout.info_verify_dialog, null)
                    v.infoDialog.text = resources.getString(R.string.create_new_account_dialog)+task.exception!!.message
                    v.buttVerifyEmail.visibility = View.GONE

                    builder.setPositiveButton(R.string.ok, ({ _: DialogInterface, _: Int ->
                    }))
                    builder.setView(v)
                        .setCancelable(false)
                        .create()
                        .show()
                }
            }
    }

    private fun signIn(email: String, password: String) {
        if (!validateForm()) {
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    updateUI(user)
                    Log.d("LOGIN", auth.currentUser?.uid.toString())
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext,
                        "Verification email sent to ${user.email} ",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = fieldEmail.text.toString()
        if (TextUtils.isEmpty(email)) {
            fieldEmail.error = "Required."
            valid = false
        } else {
            fieldEmail.error = null
        }

        val password = fieldPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            fieldPassword.error = "Required."
            valid = false
        } else {
            fieldPassword.error = null
        }

        return valid
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            if (user.isEmailVerified) {
                val intentMenu: Intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intentMenu)
            } else {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@LoginActivity);
                val inflater: LayoutInflater = LayoutInflater.from(this@LoginActivity)

                val v: View = inflater.inflate(R.layout.info_verify_dialog, null)
                v.imageViewLock.visibility = View.GONE
                v.infoDialog.text = getString(R.string.verify_email_dialog)
                v.buttVerifyEmail.setOnClickListener { sendEmailVerification() }

                builder.setView(v)
                builder.setPositiveButton(R.string.ok, ({ _: DialogInterface, _: Int ->
                }))
                builder.setCancelable(true)
                    .create()
                    .show()
            }
        }
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.createAccountButton -> createAccount(fieldEmail.text.toString(), fieldPassword.text.toString())
            R.id.signInButton -> signIn(fieldEmail.text.toString(), fieldPassword.text.toString())
        }
    }
}