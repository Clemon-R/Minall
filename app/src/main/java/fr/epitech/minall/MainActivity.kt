package fr.epitech.minall

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import fr.epitech.minall.datas.ChapterData
import fr.epitech.minall.datas.MangaData
import fr.epitech.minall.datas.UserData
import fr.epitech.minall.ui.home.HomeFragment
import fr.epitech.minall.ui.search.BoardFragment
import fr.epitech.minall.ui.search.SearchFragment
import fr.epitech.minall.utils.CacheManager


class MainActivity : AppCompatActivity() {
    companion object{
        private const val TAG = "MainActivity"
        private const val INTERNET_PERMISSION = 0
        const val CURRENT_USER = "CurrentUser"

        private lateinit var localInstance: MainActivity
        var instance: MainActivity
            get() = localInstance
            private set(value){
                localInstance = value
            }
    }

    private lateinit var currrentUser: UserData
    var user: UserData
        get() = currrentUser
        private set(value) {
            currrentUser = value
        }

    private val fragmentOpened: MutableList<Fragment> = mutableListOf()

    private fun EnableAllPermission()
    {
        if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Asking for internet permission")
            requestPermissions(arrayOf(Manifest.permission.INTERNET), INTERNET_PERMISSION)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Setting the current view and removing topbar")
        setContentView(R.layout.activity_main)
        setSupportActionBar(null)

        Log.d(TAG, "Checking all required permissions")
        this.EnableAllPermission()

        Log.d(TAG, "Setting up the cache")
        CacheManager.setup(applicationContext)
        user = CacheManager.instance.loadClass(CURRENT_USER) ?: UserData()
        instance = this

        Log.d(TAG, "Setting up the left nav bar")
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.addDrawerListener(object: DrawerLayout.DrawerListener{
            override fun onDrawerStateChanged(newState: Int) {
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerClosed(drawerView: View) {
            }

            override fun onDrawerOpened(drawerView: View) {
                val lblMangaReaded = findViewById<TextView>(R.id.lblMangaReaded)
                val lblChapterNotReaded = findViewById<TextView>(R.id.lblChapterNotReaded)
                val lblChapterReaded = findViewById<TextView>(R.id.lblChapterReaded)

                lblMangaReaded.text = "${user.mangaSlugFollowed.size} Mangas followed"
                var nbr = 0
                for (slug in user.mangaSlugFollowed)
                    nbr += slug.value.size
                lblChapterReaded.text = "$nbr Chapters readed"
                for (slug in user.mangaSlugFollowed) {
                    val manga = CacheManager.instance.loadClass<MangaData>(slug.key) ?: continue
                    nbr -= manga.chapters.size
                }
                nbr *= -1
                lblChapterNotReaded.text = "$nbr Chapters to read"
            }

        })
        val deployMenu: ImageButton = findViewById(R.id.deployMenu)
        deployMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val closeMenu: ImageButton = findViewById(R.id.closeMenu)
        closeMenu.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setCheckedItem(R.id.nav_home)
        navView.setNavigationItemSelectedListener { item ->
            var result = true
            if (!item.isChecked) {
                when (item.itemId) {
                    R.id.nav_home -> changeFragment(HomeFragment())
                    R.id.nav_search -> changeFragment(SearchFragment())
                    R.id.nav_board -> changeFragment(BoardFragment())
                    else -> result = false
                }
                item.isChecked = result
            } else
                result = false
            result
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    fun addToStack(fragment: Fragment){
        if (fragmentOpened.contains(fragment))
            return
        fragmentOpened += fragment
    }

    fun openFragment(fragment: Fragment)
    {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        transaction.addToBackStack(fragment.tag)
        transaction.commit()
        addToStack(fragment)
    }

    fun changeFragment(fragment: Fragment)
    {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        for (view in fragmentOpened) {
            val removeTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            removeTransaction.remove(view)
            removeTransaction.commitNow()
        }
        fragmentOpened.clear()
        transaction.commit()
        addToStack(fragment)
    }

    fun chapterHasBeenReaded(manga: MangaData, chapter: ChapterData) : Boolean
    {
        val result = user.mangaSlugFollowed[manga.slug] ?: return false
        return result.contains(chapter.id)
    }

    fun chapterReaded(manga: MangaData, chapter: ChapterData)
    {
        val slug = user.mangaSlugFollowed[manga.slug] ?: return
        slug.add(chapter.id)
        CacheManager.instance.saveClass(CURRENT_USER, user)
    }

    fun followManga(manga: MangaData)
    {
        if (user.mangaSlugFollowed.containsKey(manga.slug))
            return
        user.mangaSlugFollowed[manga.slug] = mutableListOf()
        CacheManager.instance.saveClass(CURRENT_USER, user)
    }

    fun unfollowManga(manga: MangaData)
    {
        if (!user.mangaSlugFollowed.containsKey(manga.slug))
            return
        user.mangaSlugFollowed.remove(manga.slug)
        CacheManager.instance.saveClass(CURRENT_USER, user)
    }
}
