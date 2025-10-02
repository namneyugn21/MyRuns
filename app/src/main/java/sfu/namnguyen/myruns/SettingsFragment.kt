package sfu.namnguyen.myruns

import android.content.Intent
import android.os.Bundle
import android.net.Uri // For opening the webpage
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

// In order to make the preference layout similar to the demo and to follow best practice, I have implement similar to this docs
// https://medium.com/google-developer-experts/exploring-android-jetpack-preferences-8bcb0b7bdd14
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // find the "Account Preferences" item and set its click listener
        findPreference<Preference>("account_profile")?.setOnPreferenceClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
            true
        }

        // find the "Webpage" item and set its click listener
        findPreference<Preference>("webpage_link")?.setOnPreferenceClickListener {
            val url = "https://www.sfu.ca/computing.html"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
            true
        }
    }
}