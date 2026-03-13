package pl.mrugacz95.kiosk


import android.app.admin.DevicePolicyManager
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.UserManager
import android.provider.Settings
import android.text.Editable
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pl.mrugacz95.kiosk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mAdminComponentName: ComponentName
    private lateinit var mDevicePolicyManager: DevicePolicyManager
    private lateinit var binding: ActivityMainBinding

    var firststart = true



    val Context.dataStore by preferencesDataStore(name = "settings")

    val URLKEY = stringPreferencesKey("urlkey")


    companion object {
        const val LOCK_ACTIVITY_KEY = "pl.mrugacz95.kiosk.MainActivity"
    }

    suspend fun saveURL(context: Context, url: String){
        context.dataStore.edit { preferences ->
            preferences[URLKEY] = url
        }
    }


    suspend fun readUrl(context: Context): String? {
        val preferences = context.dataStore.data.first()
        return preferences[URLKEY]
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mAdminComponentName = MyDeviceAdminReceiver.getComponentName(this)
        mDevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        mDevicePolicyManager.removeActiveAdmin(mAdminComponentName)

        val isAdmin = isAdmin()
        if (isAdmin) {
            Snackbar.make(binding.content, R.string.device_owner, Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.content, R.string.not_device_owner, Snackbar.LENGTH_SHORT).show()
        }


        lifecycleScope.launch {


            val urltoreplace = readUrl(this@MainActivity)
            val edittext = findViewById<EditText>(R.id.editTextText2)
            edittext.setText(urltoreplace)
            autolaunchonstart()




        }
        binding.btStartLockTask.setOnClickListener {
            setKioskPolicies(true, isAdmin)
        }
        binding.btStopLockTask.setOnClickListener {
            setKioskPolicies(false, isAdmin)
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            intent.putExtra(LOCK_ACTIVITY_KEY, false)
            startActivity(intent)
        }

        binding.btstartwbvuiew.setOnClickListener {
            launchwebviewwithstring()
        }




    }

    private fun isAdmin() = mDevicePolicyManager.isDeviceOwnerApp(packageName)

    private fun launchwebviewwithstring() {
        val edittext = findViewById<EditText>(R.id.editTextText2)
        val urlstring = edittext.text.toString()

        lifecycleScope.launch {
            saveURL(this@MainActivity, urlstring)
        }

        launchWebView(urlstring)
    }

    private fun autolaunchonstart() {
        if (firststart) {
            val isAdmin = isAdmin()
            //setKioskPolicies(true, isAdmin)
            //launchwebviewwithstring()
            firststart = false
            Log.v("Autosart fun", "Autolaunching")
        }
        else {
            Log.v("Autosart fun", "not first start skipping auto launch")
        }



    }

    private fun launchWebView(url: String) {
        setContentView(R.layout.webview_activity)

        val button = findViewById<Button>(R.id.webviewclosebtn)

        var leaveClicks = 0
        button.setOnClickListener {
            // do something
            leaveClicks++
            Log.v("Leavebutton", "leave button clicked")
            if (leaveClicks > 25) {
                setContentView(R.layout.activity_main)

                lifecycleScope.launch {


                    val urltoreplace = readUrl(this@MainActivity)
                    val edittext = findViewById<EditText>(R.id.editTextText2)
                    edittext.setText(urltoreplace)
                    autolaunchonstart()




                }
                val isAdmin = isAdmin()
                val btStartLockTask = findViewById<Button>(R.id.btStartLockTask)
                btStartLockTask.setOnClickListener {
                    setKioskPolicies(true, isAdmin)
                }
                val btStopLockTask = findViewById<Button>(R.id.btStopLockTask)
                btStopLockTask.setOnClickListener {
                    setKioskPolicies(false, isAdmin)
                    val intent = Intent(applicationContext, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    intent.putExtra(LOCK_ACTIVITY_KEY, false)
                    startActivity(intent)
                }

                val btstartwbvuiew = findViewById<Button>(R.id.btstartwbvuiew)
                btstartwbvuiew.setOnClickListener {
                    val edittext = findViewById<EditText>(R.id.editTextText2)
                    val urlstring = edittext.text.toString()
                    launchWebView(urlstring)
                }

            }

        }

        val myWebView: WebView = findViewById(R.id.mainwebviewwin)
        myWebView.webViewClient = WebViewClient()

        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.domStorageEnabled = true
        myWebView.settings.useWideViewPort = true
        myWebView.settings.loadWithOverviewMode = true


        myWebView.loadUrl(url)
        myWebView.settings.blockNetworkLoads = false

    }

    private fun setKioskPolicies(enable: Boolean, isAdmin: Boolean) {
        if (isAdmin) {
            setRestrictions(enable)
            enableStayOnWhilePluggedIn(enable)
            setUpdatePolicy(enable)
            setAsHomeApp(enable)
            setKeyGuardEnabled(enable)
        }
        setLockTask(enable, isAdmin)
        setImmersiveMode(enable)
    }

    // region restrictions
    private fun setRestrictions(disallow: Boolean) {
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, disallow)
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, disallow)
        setUserRestriction(UserManager.DISALLOW_ADD_USER, disallow)
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, disallow)
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, disallow)
        mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, disallow)
    }

    private fun setUserRestriction(restriction: String, disallow: Boolean) = if (disallow) {
        mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction)
    } else {
        mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction)
    }
    // endregion

    private fun enableStayOnWhilePluggedIn(active: Boolean) = if (active) {
        mDevicePolicyManager.setGlobalSetting(
            mAdminComponentName,
            Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
            (BatteryManager.BATTERY_PLUGGED_AC
                    or BatteryManager.BATTERY_PLUGGED_USB
                    or BatteryManager.BATTERY_PLUGGED_WIRELESS).toString()
        )
    } else {
        mDevicePolicyManager.setGlobalSetting(mAdminComponentName, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, "0")
    }

    private fun setLockTask(start: Boolean, isAdmin: Boolean) {
        if (isAdmin) {
            mDevicePolicyManager.setLockTaskPackages(
                mAdminComponentName, if (start) arrayOf(packageName) else arrayOf()
            )
        }
        if (start) {
            startLockTask()
        } else {
            stopLockTask()
        }
    }

    private fun setUpdatePolicy(enable: Boolean) {
        if (enable) {
            mDevicePolicyManager.setSystemUpdatePolicy(
                mAdminComponentName,
                SystemUpdatePolicy.createWindowedInstallPolicy(60, 120)
            )
        } else {
            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, null)
        }
    }

    private fun setAsHomeApp(enable: Boolean) {
        if (enable) {
            val intentFilter = IntentFilter(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            mDevicePolicyManager.addPersistentPreferredActivity(
                mAdminComponentName, intentFilter, ComponentName(packageName, MainActivity::class.java.name)
            )
        } else {
            mDevicePolicyManager.clearPackagePersistentPreferredActivities(
                mAdminComponentName, packageName
            )
        }
    }

    private fun setKeyGuardEnabled(enable: Boolean) {
        mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, !enable)
    }

    @Suppress("DEPRECATION")
    private fun setImmersiveMode(enable: Boolean) {
        if (enable) {
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.decorView.systemUiVisibility = flags
        } else {
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            window.decorView.systemUiVisibility = flags
        }
    }



    private fun installApp() {
        if (!isAdmin()) {
            Snackbar.make(binding.content, R.string.not_device_owner, Snackbar.LENGTH_LONG).show()
            return
        }

    }
}
