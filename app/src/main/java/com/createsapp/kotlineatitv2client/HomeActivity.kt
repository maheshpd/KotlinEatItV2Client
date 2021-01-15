package com.createsapp.kotlineatitv2client

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.createsapp.kotlineatitv2client.common.Common
import com.createsapp.kotlineatitv2client.database.CartDataSource
import com.createsapp.kotlineatitv2client.database.CartDatabase
import com.createsapp.kotlineatitv2client.database.LocalCartDataSource
import com.createsapp.kotlineatitv2client.eventbus.*
import com.createsapp.kotlineatitv2client.model.CategoryModel
import com.createsapp.kotlineatitv2client.model.FoodModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.app_bar_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeActivity : AppCompatActivity() {

    //trendinwealth@gmail.com

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var cartDataSource: CartDataSource
    private lateinit var navController: NavController
    private var drawer: DrawerLayout? = null
    private var dialog: AlertDialog? = null

    override fun onResume() {
        super.onResume()
        countCartItem()
    }

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()

        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(this).cartDAO())

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        navController = findNavController(R.id.nav_host_fragment)


        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            navController.navigate(R.id.nav_cart)
        }
        drawer = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_menu, R.id.nav_food_detail,
                R.id.nav_cart
            ), drawer
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        var headerView = navView.getHeaderView(0)
        var txt_user = headerView.findViewById<TextView>(R.id.txt_user)
        Common.setSpanString("Hey", Common.currentUser!!.uid, txt_user)

        navView.setNavigationItemSelectedListener { item ->

            item.isChecked = true
            drawer!!.closeDrawers()

            when (item.itemId) {
                R.id.nav_sign_out -> {
                    signOut()
                }
                R.id.nav_home -> {
                    navController.navigate(R.id.nav_home)
                }
                R.id.nav_cart -> {
                    navController.navigate(R.id.nav_cart)
                }
                R.id.nav_menu -> {
                    navController.navigate(R.id.nav_menu)
                }
            }
            true
        }
        countCartItem()

    }

    private fun signOut() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Sign out")
            .setMessage("Doy you really want to exit?")
            .setNegativeButton("CANCEL") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("OK") { _, _ ->
                Common.foodSelected = null
                Common.categorySelected = null
                Common.currentUser = null
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this@HomeActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCategorySelected(event: CategoryClick) {
        if (event.isSuccess) {
//            Toast.makeText(this, "Click to" + event.category.name, Toast.LENGTH_SHORT).show()
            findNavController(R.id.nav_host_fragment).navigate(R.id.nav_food_list)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onFoodSelected(event: FoodItemClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment).navigate(R.id.nav_food_detail)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCountCartEvent(event: FoodItemClick) {
        if (event.isSuccess) {
            countCartItem()
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onHideFABEVENT(event: HideFABCart) {
        if (event.isHide) {
            fab.hide()
        } else
            fab.show()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onPopularFoodItemClick(event: PopularFoodItemClick) {
        if (event.popularCategoryModel != null) {
            dialog!!.show()
            FirebaseDatabase.getInstance()
                .getReference("Category")
                .child(event.popularCategoryModel.menu_id!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        dialog!!.dismiss()
                        Toast.makeText(this@HomeActivity, "" + p0.message, Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            Common.categorySelected = p0.getValue(CategoryModel::class.java)

                            //Load food
                            FirebaseDatabase.getInstance()
                                .getReference("Category")
                                .child(event.popularCategoryModel.menu_id!!)
                                .child("foods")
                                .orderByChild("id")
                                .equalTo(event.popularCategoryModel.food_id)
                                .limitToLast(1)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {
                                        dialog!!.dismiss()
                                        Toast.makeText(
                                            this@HomeActivity,
                                            "" + p0.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {
                                        if (p0.exists()) {
                                            for (foodSnapshot in p0.children)
                                                Common.foodSelected =
                                                    foodSnapshot.getValue(FoodModel::class.java)
                                            navController.navigate(R.id.nav_food_detail)
                                        } else {

                                            Toast.makeText(
                                                this@HomeActivity,
                                                "Item doesn't exists",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        dialog!!.dismiss()
                                    }

                                })
                        } else {
                            dialog!!.dismiss()
                            Toast.makeText(
                                this@HomeActivity,
                                "Item doesn't exists",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onBestDealFoodItemClick(event: BestDealItemClick) {
        if (event.model != null) {
            dialog!!.show()
            FirebaseDatabase.getInstance()
                .getReference("Category")
                .child(event.model.menu_id!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        dialog!!.dismiss()
                        Toast.makeText(this@HomeActivity, "" + p0.message, Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            Common.categorySelected = p0.getValue(CategoryModel::class.java)

                            //Load food
                            FirebaseDatabase.getInstance()
                                .getReference("Category")
                                .child(event.model.menu_id!!)
                                .child("foods")
                                .orderByChild("id")
                                .equalTo(event.model.food_id)
                                .limitToLast(1)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {
                                        dialog!!.dismiss()
                                        Toast.makeText(
                                            this@HomeActivity,
                                            "" + p0.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {
                                        if (p0.exists()) {
                                            for (foodSnapshot in p0.children)
                                                Common.foodSelected =
                                                    foodSnapshot.getValue(FoodModel::class.java)
                                            navController.navigate(R.id.nav_food_detail)
                                        } else {

                                            Toast.makeText(
                                                this@HomeActivity,
                                                "Item doesn't exists",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        dialog!!.dismiss()
                                    }

                                })
                        } else {
                            dialog!!.dismiss()
                            Toast.makeText(
                                this@HomeActivity,
                                "Item doesn't exists",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        }
    }

    private fun countCartItem() {
        cartDataSource.countItemInCart(Common.currentUser?.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Int> {
                override fun onSuccess(t: Int) {
                    fab.count = t
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty"))
                        Toast.makeText(
                            this@HomeActivity,
                            "[COUNT CART]" + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    else
                        fab.count = 0
                }

            })
    }

}
