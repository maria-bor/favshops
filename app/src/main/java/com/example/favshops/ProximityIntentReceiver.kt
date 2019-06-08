package com.example.favshops

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log
import android.app.*
import android.graphics.Color
import android.support.v4.app.NotificationCompat

class ProximityIntentReceiver: BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "com.example.favshops.UPDATE"
        var ID = 0
    }

    override fun onReceive(context: Context?, i: Intent?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(context, MainActivity::class.java)
        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)

        val notiPendingIntent = stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_custom)
            .setColor(Color.BLUE)
            .setContentTitle("You're near to " + i?.getStringExtra("Shop_name"))
            .setContentIntent(notiPendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(ID++, builder.build())
    }
}