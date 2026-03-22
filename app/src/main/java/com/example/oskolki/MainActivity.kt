package com.example.oskolki

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import android.content.Intent
import com.example.Oskolki.R

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

            // Простая проверка на пустые поля
            if (email.isEmpty() || password.isEmpty()) {
                // Показать сообщение об ошибке (можно добавить Toast)
                android.widget.Toast.makeText(this, "Заполните все поля", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                // Здесь будет логика входа
                android.widget.Toast.makeText(this, "Вход выполняется...", android.widget.Toast.LENGTH_SHORT).show()
                // TODO: Добавить авторизацию
            }
        }
        // Кнопка Google
        findViewById<AppCompatButton>(R.id.btn_google).setOnClickListener {
            // Здесь логика для входа через Google
            android.widget.Toast.makeText(this, "Вход через Google", android.widget.Toast.LENGTH_SHORT).show()
            // TODO: Добавить Google авторизацию
        }
        // Текст "Зарегистрироваться"
        findViewById<TextView>(R.id.tv_register).setOnClickListener {
            // Здесь логика для регистрации
            android.widget.Toast.makeText(this, "Переход к регистрации", android.widget.Toast.LENGTH_SHORT).show()
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