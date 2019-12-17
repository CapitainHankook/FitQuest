package ru.tzhack.facegame.welcome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import ru.tzhack.facegame.MainActivity

//TODO: ЗАДАНИЕ #1
/**
 * Задание №1.
 *
 * Создание Splash Screen'a.
 *
 * Splash Screen - отображается пользователю во время "холодного"
 *                 запуска приложения (первичная инициализация).
 *
 * Т.к. нам инициализировать нечего, будем имитировать процесс
 * инициализации приложения (пусть будет 500-700 мс).
 *
 * 1. Изучить объект [android.os.Handler]. Разобраться, как с его
 *    помощью "заставить" приложение немного подождать, не блокируя
 *    главный поток приложения.
 *
 * 2. На данном экране необходимо произвести проверку на наличие
 *    сохраненного URL удаленного сервера.
 *    (Для проверки использовать сервис [StorageService], доступ
 *    к нему осуществлять через объект [App])
 *
 *     а. В случае его отсутствия сразу перекидываем пользователя
 *     на экран настроек, где будет произведена первичная настройка
 *     приложения.
 *
 *     б. В случае, если URL удаленного сервера был введен ранее,
 *     имитируем инициализацию приложения и переходим на экран
 *     с поиском пользователей.
 * */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
        }, 400)
    }
}
