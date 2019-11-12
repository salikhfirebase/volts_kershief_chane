package volts.kershief.chane

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import volts.kershief.chane.fragments.MainInfoLaLaFragment
import volts.kershief.chane.fragments.SignLaLaUpFragment
import volts.kershief.chane.fragments.SingInMoreFragment

class QuestionaireMainQwerActivity : AppCompatActivity() {


    private var fragmentMain = androidx.fragment.app.Fragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionaire_main_qwer)

        if (intent.getStringExtra("action") == "registration") {
            fragmentMain = SignLaLaUpFragment()
        }

        if (intent.getStringExtra("action") == "facebook_login"){
            fragmentMain = MainInfoLaLaFragment()
        }

        if (intent.getStringExtra("action") == "sign_in") {
            fragmentMain = SingInMoreFragment()
        }

        var useless = 1

        for (i in 0..10) {
            useless++
        }

        uselessFun()

        setFragment(fragmentMain)
    }

    private fun setFragment(f: androidx.fragment.app.Fragment) {

        val fm: androidx.fragment.app.FragmentManager = supportFragmentManager
        val ft: androidx.fragment.app.FragmentTransaction = fm.beginTransaction()

        ft.replace(R.id.question_container, f)
        ft.commit()

    }

    fun uselessFun() {
        var i = 0

        i++
    }
}
