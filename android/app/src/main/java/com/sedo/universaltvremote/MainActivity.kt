package com.sedo.universaltvremote

import android.app.Activity
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var nsd: NsdManager
    private val listeners = mutableListOf<NsdManager.DiscoveryListener>()
    private val seen = linkedMapOf<String, String>()
    private lateinit var status: TextView
    private lateinit var devices: TextView
    private lateinit var scanButton: Button
    private val green = Color.rgb(166, 242, 105)
    private val background = Color.rgb(16, 19, 28)
    private val panel = Color.rgb(29, 34, 47)
    private val white = Color.rgb(244, 246, 250)
    private val muted = Color.rgb(165, 173, 190)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nsd = getSystemService(Context.NSD_SERVICE) as NsdManager
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 28, 22, 20)
            setBackgroundColor(background)
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 24)
        }
        val title = TextView(this).apply {
            text = "Universal TV Remote"
            textSize = 26f
            setTextColor(green)
            gravity = Gravity.CENTER
        }
        content.addView(title, matchWrap())
        val subtitle = TextView(this).apply {
            text = "ريموت Android TV • اكتشاف محلي"
            textSize = 17f
            setTextColor(white)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 24)
        }
        content.addView(subtitle, matchWrap())
        status = TextView(this).apply {
            text = "جاهز للبحث. تأكد من اتصال الهاتف والتلفاز بشبكة Wi-Fi نفسها."
            textSize = 15f
            setTextColor(white)
            setPadding(16, 16, 16, 16)
            setBackgroundColor(panel)
        }
        content.addView(status, matchWrap())
        scanButton = button("البحث عن أجهزة التلفاز") { startDiscovery() }
        content.addView(scanButton, matchWrap())
        val section = TextView(this).apply {
            text = "الأجهزة المكتشفة"
            textSize = 20f
            setTextColor(green)
            setPadding(0, 24, 0, 8)
        }
        content.addView(section, matchWrap())
        devices = TextView(this).apply {
            text = "لم يتم البحث بعد."
            textSize = 14f
            setTextColor(muted)
            setPadding(16, 16, 16, 16)
            setBackgroundColor(panel)
        }
        content.addView(devices, matchWrap())

        val caution = TextView(this).apply {
            text = "ملاحظة: اكتشاف الجهاز ليس اقترانًا. التحكم والكتابة يتطلبان تنفيذ بروتوكول Android TV Remote المدعوم من التلفاز والتحقق من الاقتران. لن نرسل أوامر غير موثقة ولن نخزن كلمات مرور."
            textSize = 13f
            setTextColor(muted)
            setPadding(2, 18, 2, 12)
        }
        content.addView(caution, matchWrap())

        content.addView(TextView(this).apply {
            text = "الكتابة على التلفاز"
            textSize = 20f
            setTextColor(green)
            setPadding(0, 18, 0, 8)
        }, matchWrap())
        val keyboard = EditText(this).apply {
            hint = "اكتب بالعربية أو English (واجهة أولية)"
            setHintTextColor(muted)
            setTextColor(white)
            textSize = 16f
            setSingleLine(true)
            setBackgroundColor(panel)
        }
        content.addView(keyboard, matchWrap())
        content.addView(button("إرسال النص إلى التلفاز") {
            Toast.makeText(this, "الإرسال غير مفعّل بعد؛ يلزم إكمال الاقتران وبروتوكول الكتابة أولًا.", Toast.LENGTH_LONG).show()
        }, matchWrap())

        content.addView(TextView(this).apply {
            text = "الريموت"
            textSize = 20f
            setTextColor(green)
            setPadding(0, 22, 0, 8)
        }, matchWrap())
        val grid = arrayOf(
            arrayOf("⏻", "↑", "⌂"),
            arrayOf("←", "موافق", "→"),
            arrayOf("رجوع", "↓", "كتم"),
            arrayOf("الصوت −", "الصوت +", "YouTube")
        )
        grid.forEach { row ->
            val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row.forEach { label ->
                val b = button(label) {
                    Toast.makeText(this, "زر الواجهة فقط: سيُفعّل بعد تنفيذ بروتوكول الاتصال الآمن.", Toast.LENGTH_SHORT).show()
                }
                val params = LinearLayout.LayoutParams(0, dp(54), 1f)
                params.setMargins(dp(3), dp(3), dp(3), dp(3))
                line.addView(b, params)
            }
            content.addView(line, matchWrap())
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        textSize = 14f
        setTextColor(Color.rgb(18, 24, 15))
        setBackgroundColor(green)
        setOnClickListener { action() }
    }

    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2)
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun startDiscovery() {
        stopDiscovery()
        seen.clear()
        devices.text = "جارٍ البحث عن خدمات Android TV على الشبكة المحلية…"
        status.text = "بدأ البحث عن خدمات الريموت المعروفة. قد لا تظهر بعض الأجهزة إذا لم تعلن الخدمة."
        scanButton.isEnabled = false
        val types = listOf("_androidtvremote2._tcp.", "_androidtvremote._tcp.")
        types.forEach { type ->
            val listener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(serviceType: String) {}
                override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                    runOnUiThread {
                        val key = "${serviceInfo.serviceName}|${serviceInfo.serviceType}"
                        if (!seen.containsKey(key)) {
                            seen[key] = "${serviceInfo.serviceName}\nالخدمة: ${serviceInfo.serviceType}\nيجري التحقق من تفاصيل الاتصال…"
                            devices.text = seen.values.joinToString("\n\n")
                        }
                        try {
                            nsd.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                                override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {
                                    runOnUiThread {
                                        seen[key] = "${serviceInfo.serviceName}\nالخدمة: ${serviceInfo.serviceType}\nتعذر قراءة عنوان الخدمة (رمز $errorCode)."
                                        devices.text = seen.values.joinToString("\n\n")
                                    }
                                }
                                override fun onServiceResolved(info: NsdServiceInfo) {
                                    runOnUiThread {
                                        seen[key] = "${info.serviceName}\nالخدمة: ${info.serviceType}\nالعنوان المحلي: ${info.host?.hostAddress ?: "غير معروف"}\nالمنفذ: ${info.port}\nالحالة: اكتشاف فقط — لم يتم الاقتران."
                                        devices.text = seen.values.joinToString("\n\n")
                                    }
                                }
                            })
                        } catch (_: Exception) {}
                    }
                }
                override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                    runOnUiThread {
                        seen.remove("${serviceInfo.serviceName}|${serviceInfo.serviceType}")
                        devices.text = if (seen.isEmpty()) "فُقد إعلان الخدمة أو لم تعد ظاهرة." else seen.values.joinToString("\n\n")
                    }
                }
                override fun onDiscoveryStopped(serviceType: String) {}
                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    runOnUiThread {
                        status.text = "فشل بدء البحث عن $serviceType (رمز $errorCode)."
                        try { nsd.stopServiceDiscovery(this) } catch (_: Exception) {}
                    }
                }
                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                    try { nsd.stopServiceDiscovery(this) } catch (_: Exception) {}
                }
            }
            listeners.add(listener)
            try { nsd.discoverServices(type, NsdManager.PROTOCOL_DNS_SD, listener) } catch (_: Exception) {}
        }
        status.postDelayed({
            scanButton.isEnabled = true
            if (seen.isEmpty()) {
                devices.text = "لم يُكتشف أي إعلان خدمة بعد. هذا لا يثبت أن التلفاز غير مدعوم؛ قد تختلف خدمة الريموت أو إعدادات الشبكة."
                status.text = "انتهى وقت البحث الأولي. جرّب تشغيل خدمة Android TV Remote Service في التلفاز ثم أعد البحث."
            } else {
                status.text = "تم العثور على ${seen.size} إعلان خدمة. يلزم الاقتران قبل إرسال أوامر التحكم."
            }
        }, 12000)
    }

    private fun stopDiscovery() {
        listeners.toList().forEach {
            try { nsd.stopServiceDiscovery(it) } catch (_: Exception) {}
        }
        listeners.clear()
    }

    override fun onDestroy() {
        stopDiscovery()
        super.onDestroy()
    }
}
