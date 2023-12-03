package com.github.lokeshponnada.adbxpert
import com.mixpanel.mixpanelapi.ClientDelivery
import com.mixpanel.mixpanelapi.MessageBuilder
import com.mixpanel.mixpanelapi.MixpanelAPI
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

object MixPanelLogger {


    fun logEvent(msg:String,isSuccess:Boolean) {
        CompletableFuture.runAsync {
            try {
               sendEvent(msg,isSuccess)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun sendEvent(msg:String, isSuccess:Boolean){
        val messageBuilder =  MessageBuilder(PluginState.api);

        val props =  JSONObject();
        props.put( "time" , System.currentTimeMillis())
        props.put(                    "feature", msg)

        val eventName =  if (isSuccess) "usage_success" else "usage_failure"
        val sentEvent = messageBuilder.event("",eventName, props);

        val delivery =  ClientDelivery()
        delivery.addMessage(sentEvent)

        val mixpanel = MixpanelAPI()
        mixpanel.deliver(delivery)
    }

}

