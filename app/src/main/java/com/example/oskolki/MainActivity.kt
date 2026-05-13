package com.example.oskolki

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.oskolki.model.GoogleAuthAndroidRequest
import com.example.oskolki.model.LoginRequest
import com.example.oskolki.network.RetrofitClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var googleSignInClient: GoogleSignInClient? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK || result.data == null) {
            Log.d(TAG, "Google Sign-In cancelled or no data")
            return@registerForActivityResult
        }
        GoogleSignIn.getSignedInAccountFromIntent(result.data)
            .addOnSuccessListener { account ->
                Log.d(TAG, "Google Sign-In success: email=${account.email}")
                account.idToken?.let { sendIdTokenToBackend(it) }
                    ?: run {
                        Log.w(TAG, "Google Sign-In: idToken is null")
                        Toast.makeText(this, "Ошибка: не получен ID token", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Google Sign-In failed: ${e.localizedMessage}")
                Toast.makeText(this, "Google Sign-In отменён: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<TextView>(R.id.tv_forgot_password).setOnClickListener {
            // TODO: Forgot password
        }

        findViewById<AppCompatButton>(R.id.btn_login).setOnClickListener {
            val email = findViewById<EditText>(R.id.et_email).text.toString()
            val password = findViewById<EditText>(R.id.et_password).text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "Email login attempt: $email")
                lifecycleScope.launch {
                    try {
                        val response =
                            RetrofitClient.apiService.login(LoginRequest(email, password))
                        Log.d(
                            TAG,
                            "Email login success: userId=${response.user.id}, token=${
                                response.token.take(20)
                            }..."
                        )
                        saveSession(response.token, response.user.id)
                        Toast.makeText(this@MainActivity, "Вход выполнен", Toast.LENGTH_SHORT)
                            .show()
                        navigateToMap()
                    } catch (e: Exception) {
                        Log.e(TAG, "Email login failed: ${e.message}")
                        Toast.makeText(
                            this@MainActivity,
                            "Ошибка входа: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        findViewById<AppCompatButton>(R.id.btn_google).setOnClickListener {
            Log.d(TAG, "Google Sign-In button clicked")
            googleSignInClient?.signInIntent?.let { intent ->
                googleSignInLauncher.launch(intent)
            } ?: Log.e(TAG, "googleSignInClient is null")
        }

        findViewById<TextView>(R.id.tv_register).setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun sendIdTokenToBackend(idToken: String) {
        Log.d(TAG, "Sending idToken to /api/auth/google/android (len=${idToken.length})")
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.googleAuthAndroid(
                    GoogleAuthAndroidRequest(idToken)
                )
                Log.d(TAG, "Google auth success: userId=${response.user.id}, token=${response.token.take(20)}...")
                saveSession(response.token, response.user.id)
                Toast.makeText(this@MainActivity, "Вход выполнен", Toast.LENGTH_SHORT).show()
                navigateToMap()
            } catch (e: Exception) {
                Log.e(TAG, "Google auth backend failed: ${e.message}")
                Toast.makeText(this@MainActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveSession(token: String, userId: String) {
        getSharedPreferences("auth_prefs", MODE_PRIVATE).edit()
            .putString("token", token)
            .putString("user_id", userId)
            .apply()
    }

    private fun navigateToMap() {
        val intent = Intent(this, MapActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
