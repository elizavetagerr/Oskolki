package com.example.oskolki

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.example.Oskolki.R

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        // Кнопка регистрации
        findViewById<AppCompatButton>(R.id.btn_reg).setOnClickListener {
            val email = findViewById<EditText>(R.id.et_email_reg).text.toString()
            val password = findViewById<EditText>(R.id.et_password_reg).text.toString()
            val confirmPassword = findViewById<EditText>(R.id.et_confirm_password_reg).text.toString()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                android.widget.Toast.makeText(this, "Заполните все поля", android.widget.Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                android.widget.Toast.makeText(this, "Пароли не совпадают", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(this, "Регистрация успешна!", android.widget.Toast.LENGTH_SHORT).show()
                // Возврат на экран входа
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        // Текст "Войти" для возврата на экран входа
        findViewById<TextView>(R.id.tv_login_back).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}