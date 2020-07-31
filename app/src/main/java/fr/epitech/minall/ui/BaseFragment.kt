package fr.epitech.minall.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import fr.epitech.minall.R
import fr.epitech.minall.network.HttpClient

abstract class BaseFragment : Fragment() {

    private var drawerLayout: DrawerLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        drawerLayout = activity?.findViewById(R.id.drawer_layout)
        return null
    }

    override fun onResume() {
        super.onResume()
        drawerLayout?.closeDrawer(GravityCompat.START)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        HttpClient.instance.removeAllRequests(this)
    }
}