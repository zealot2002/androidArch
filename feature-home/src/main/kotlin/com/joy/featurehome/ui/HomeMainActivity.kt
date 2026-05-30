package com.joy.featurehome.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.joy.common.base.BaseActivity
import com.joy.common.router.RouterConstants
import com.joy.featurehome.R
import com.joy.featurehome.databinding.ActivityHomeMainBinding

@Route(path = RouterConstants.HOME_MAIN)
class HomeMainActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdgeInsets(binding.root)

        if (savedInstanceState == null) {
            switchFragment(HomeFragment(), HomeFragment::class.java.simpleName)
            binding.bottomNav.selectedItemId = R.id.nav_home
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_social -> SocialFragment()
                R.id.nav_cart -> CartFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> return@setOnItemSelectedListener false
            }
            switchFragment(fragment, fragment::class.java.simpleName)
            true
        }
    }

    private fun switchFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment, tag)
            .commit()
    }
}
