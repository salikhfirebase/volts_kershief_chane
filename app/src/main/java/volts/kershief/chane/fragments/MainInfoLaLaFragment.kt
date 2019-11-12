package volts.kershief.chane.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import volts.kershief.chane.Model.User
import volts.kershief.chane.R
import volts.kershief.chane.db.AppDatabase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 *
 */
class MainInfoLaLaFragment : Fragment() {

    private var the_fragmentMain = androidx.fragment.app.Fragment()
    private lateinit var the_nickEdit: EditText
    private lateinit var the_yearsSpinner: Spinner
    private lateinit var the_daysSpinner: Spinner
    private lateinit var the_monthsSpinner: Spinner
    private lateinit var the_raceSpinner: Spinner
    private lateinit var the_heightSeek: SeekBar
    private lateinit var the_weightSeek: SeekBar
    lateinit var the_heightText: TextView
    lateinit var the_weightText: TextView
    private lateinit var the_nextButton: Button
    lateinit var db: AppDatabase

    var the_user = User()
    private var the_birthDate = ""
    var the_userHeight = ""
    var the_userWeight = ""
    private var the_userId = 0


    @SuppressLint("CheckResult")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_info_la_la, container, false)

        var useless = 1

        for (i in 0..10) {
            useless++
        }

        uselessFun()

        the_nickEdit = view.findViewById(R.id.nick_edit_text_prampam)
        the_yearsSpinner = view.findViewById(R.id.years_old_but_gold_spinner)
        the_daysSpinner = view.findViewById(R.id.days_again_spinner)
        the_monthsSpinner = view.findViewById(R.id.mesyac_again_spinner)
        the_raceSpinner = view.findViewById(R.id.race_spinner_for_three_races)
        the_heightSeek = view.findViewById(R.id.height_pes_bar)
        the_weightSeek = view.findViewById(R.id.weight_pes_bar)
        the_heightText = view.findViewById(R.id.chosen_height_by_user_tview)
        the_weightText = view.findViewById(R.id.weight_chosen_by_user_bar)
        the_nextButton = view.findViewById(R.id.main_info_frag_next_button)
        db = AppDatabase.getInstance(this.requireContext()) as AppDatabase
        Observable.fromCallable { db.userDao().getLastId() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                the_userId = it
            }, {
                Log.d("SaveToDb", "Didn't saved in Registration Fragment", it)
            })



        the_weightSeek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                the_weightText.text = seekBar.progress.toString()
                the_userWeight = seekBar.progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                the_weightText.text = seekBar.progress.toString()
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                the_weightText.text = seekBar.progress.toString()
            }

        })

        the_heightSeek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                the_heightText.text = (seekBar.progress + 140).toString()
                the_userHeight = (seekBar.progress + 140).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                the_heightText.text = (seekBar.progress + 140).toString()
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                the_heightText.text = (seekBar.progress + 140).toString()
            }

        })

        the_nextButton.setOnClickListener {
            userSet()
            if (the_userId != 0) {
                saveToDb()
                the_fragmentMain = ConditionsMyFragment()
                setFragment(the_fragmentMain)
            } else {
                Toast.makeText(this.requireContext(), "Подождите", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    @SuppressLint("CheckResult")
    fun userSet() {
        the_birthDate = the_daysSpinner.selectedItem.toString() + "/" + the_monthsSpinner.selectedItem.toString() + "/" + the_yearsSpinner.selectedItem.toString()
        the_user.setNick(the_nickEdit.text.toString())
        the_user.setBirth(the_birthDate)
        the_user.setRace(the_raceSpinner.selectedItem.toString())
        the_user.setHeight(the_userHeight)
        the_user.setWeight(the_userWeight)

    }

    @SuppressLint("CheckResult")
    fun saveToDb() {

        Completable.fromAction { db.userDao().updateNick(the_user.getNick(), the_userId) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, {
                Log.d("SaveToDb", "Didn't saved in Registration Fragment", it)
            })
        Completable.fromAction { db.userDao().updateBirth(the_user.getBirth(), the_userId) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, {
                Log.d("SaveToDb", "Didn't saved in Registration Fragment", it)
            })
        Completable.fromAction { db.userDao().updateRace(the_user.getRace(), the_userId) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, {
                Log.d("SaveToDb", "Didn't saved in Registration Fragment", it)
            })
        Completable.fromAction { db.userDao().updateHeight(the_user.getHeight(), the_userId) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, {
                Log.d("SaveToDb", "Didn't saved in Registration Fragment", it)
            })
        Completable.fromAction { db.userDao().updateWeight(the_user.getWeight(), the_userId) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, {
                Log.d("SaveToDb", "Didn't saved in Registration Fragment", it)
            })
    }

    private fun setFragment(f: androidx.fragment.app.Fragment) {

        val fm: androidx.fragment.app.FragmentManager = this.requireActivity().supportFragmentManager
        val ft: androidx.fragment.app.FragmentTransaction = fm.beginTransaction()

        ft.replace(R.id.question_container, f)
        ft.commit()

    }

    fun uselessFun() {
        var i = 0

        i++
    }



}
