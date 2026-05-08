package com.example.oskolki

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.oskolki.model.LoginRequest
import com.example.oskolki.network.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        // Обработчик для кнопки "Забыли пароль?"
        findViewById<TextView>(R.id.tv_forgot_password).setOnClickListener {
            // Здесь логика для восстановления пароля
            // Например, открыть экран восстановления пароля
            // TODO: Добавить логику для восстановления пароля
        }
        // Кнопка входа
        findViewById<AppCompatButton>(R.id.btn_login).setOnClickListener {
            val email = findViewById<EditText>(R.id.et_email).text.toString()
            val password = findViewById<EditText>(R.id.et_password).text.toString()
            val intent = Intent(this@MainActivity, MapActivity::class.java)
            startActivity(intent)
            finish()

            // Простая проверка на пустые поля
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.apiService.login(LoginRequest(email, password))
                        val sharedPref = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("token", response.token)
                            putString("user_id", response.user.id)
                            apply()
                        }
                        Toast.makeText(this@MainActivity, "Вход выполнен", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MainActivity, MapActivity::class.java)
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Ошибка входа: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        // Кнопка Google
        findViewById<AppCompatButton>(R.id.btn_google).setOnClickListener {
            // Здесь логика для входа через Google
            Toast.makeText(this, "Вход через Google", Toast.LENGTH_SHORT).show()
            // TODO: Добавить Google авторизацию
        }
        // Текст "Зарегистрироваться"
        findViewById<TextView>(R.id.tv_register).setOnClickListener {
            // Здесь логика для регистрации
            Toast.makeText(this, "Переход к регистрации", Toast.LENGTH_SHORT).show()
            // TODO: Открыть экран регистрации
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
