package com.createsapp.kotlineatitv2client.ui.cart

import android.app.AlertDialog
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.createsapp.kotlineatitv2client.R
import com.createsapp.kotlineatitv2client.adapter.MyCartAdapter
import com.createsapp.kotlineatitv2client.callback.IMyButtonCallback
import com.createsapp.kotlineatitv2client.common.Common
import com.createsapp.kotlineatitv2client.common.MySwipeHelper
import com.createsapp.kotlineatitv2client.database.CartDataSource
import com.createsapp.kotlineatitv2client.database.CartDatabase
import com.createsapp.kotlineatitv2client.database.LocalCartDataSource
import com.createsapp.kotlineatitv2client.eventbus.CountCartEven
import com.createsapp.kotlineatitv2client.eventbus.HideFABCart
import com.createsapp.kotlineatitv2client.eventbus.UpdateItemInCart
import com.google.android.gms.location.*
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CartFragment : Fragment() {

    private var cartDataSource: CartDataSource? = null
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var recyclerViewState: Parcelable? = null
    private lateinit var cartviewModel: CartViewModel
    private lateinit var btn_place_order: Button

    var txt_empty_cart: TextView? = null
    var txt_total_price: TextView? = null
    var group_place_holder: CardView? = null
    var recycler_cart: RecyclerView? = null
    var adapter: MyCartAdapter? = null

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCalback: LocationCallback
    private lateinit var fusedLocationProviderclient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        EventBus.getDefault().postSticky(HideFABCart(true))


        cartviewModel =
            ViewModelProviders.of(this).get(CartViewModel::class.java)

        //After create cartViewModel , init data source
        cartviewModel.initCartdataSource(requireContext())


        val root = inflater.inflate(R.layout.fragment_cart, container, false)

        initViews(root)
        initLocation()

        cartviewModel.getMutableLiveDataCartItem().observe(viewLifecycleOwner, Observer {
            if (it == null || it.isEmpty()) {
                recycler_cart!!.visibility = View.GONE
                group_place_holder!!.visibility = View.GONE
                txt_empty_cart!!.visibility = View.VISIBLE

            } else {

                recycler_cart!!.visibility = View.VISIBLE
                group_place_holder!!.visibility = View.VISIBLE
                txt_empty_cart!!.visibility = View.GONE

                adapter = MyCartAdapter(requireContext(), it)
                recycler_cart!!.adapter = adapter
            }
        })
        return root
    }

    private fun initLocation() {
        buildLocationRequest()
        buildLocationCallback()
        fusedLocationProviderclient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderclient.requestLocationUpdates(
            locationRequest, locationCalback,
            Looper.getMainLooper()
        )
    }

    private fun buildLocationCallback() {
        locationCalback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)

                currentLocation = p0!!.lastLocation
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    private fun initViews(root: View?) {

        setHasOptionsMenu(true) //Important , if not add it , menu will never be inflate

        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(requireContext()).cartDAO())

        recycler_cart = root?.findViewById(R.id.recycler_cart) as RecyclerView
        recycler_cart!!.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(context)
        recycler_cart!!.layoutManager = layoutManager
        recycler_cart!!.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))


        val swipe = object : MySwipeHelper(requireContext(), recycler_cart!!, 200) {
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(
                    MyButton(context!!,
                        "Delete",
                        30,
                        0,
                        Color.parseColor("#FF3c30"),
                        object : IMyButtonCallback {
                            override fun onClick(pos: Int) {
                                val deleteItem = adapter!!.getItemAtPosition(pos)
                                cartDataSource!!.deleteCart(deleteItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(object : SingleObserver<Int> {
                                        override fun onSubscribe(d: Disposable) {

                                        }

                                        override fun onSuccess(t: Int) {
                                            adapter!!.notifyItemRemoved(pos)
                                            sumCart()
                                            EventBus.getDefault().postSticky(CountCartEven(true))
                                            Toast.makeText(
                                                context,
                                                "Delete item success",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        override fun onError(e: Throwable) {
                                            Toast.makeText(
                                                context,
                                                "" + e.message,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    })

                            }

                        })
                )
            }
        }

        txt_empty_cart = root.findViewById(R.id.txt_empty_cart) as TextView
        txt_total_price = root.findViewById(R.id.txt_total_price) as TextView
        group_place_holder = root.findViewById(R.id.group_place_holder) as CardView

        btn_place_order = root.findViewById(R.id.btn_place_order) as Button

        //Event
        btn_place_order.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("One more step!")

            val view = LayoutInflater.from(context).inflate(R.layout.layout_place_order, null)

            val edt_address = view.findViewById<View>(R.id.edt_address) as EditText
            val edt_comment = view.findViewById<View>(R.id.edt_comment) as EditText
            val txt_address = view.findViewById<View>(R.id.txt_address_detail) as TextView
            val rdi_home = view.findViewById<View>(R.id.rdi_home_address) as RadioButton
            val rdi_other_address = view.findViewById<View>(R.id.rdi_other_address) as RadioButton
            val rdi_ship_to_this_address =
                view.findViewById<View>(R.id.rdi_ship_this_address) as RadioButton
            val rdi_cod = view.findViewById<View>(R.id.rdi_cod) as RadioButton
            val rdi_braintree = view.findViewById<View>(R.id.rdi_braintree) as RadioButton

            //Data
            edt_address.setText(Common.currentUser!!.address!!) //By default we checked rdi_home, so we will display user's address

            //Event
            rdi_home.setOnCheckedChangeListener { buttonView, isChecked ->

                if (isChecked) {
                    edt_address.setText(Common.currentUser!!.address!!)
                }
            }
            rdi_other_address.setOnCheckedChangeListener { buttonView, isChecked ->

                if (isChecked) {
                    edt_address.setText("")
                    edt_address.hint = "Enter your address"
                    txt_address.visibility = View.GONE
                }
            }
            rdi_ship_to_this_address.setOnCheckedChangeListener { buttonView, isChecked ->

                if (isChecked) {
                    fusedLocationProviderclient.lastLocation
                        .addOnFailureListener { e ->
                            txt_address.visibility = View.GONE
                            Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
                        }
                        .addOnCompleteListener { it ->
                            val coordinates = java.lang.StringBuilder()
                                .append(it.result!!.latitude)
                                .append("/")
                                .append(it.result!!.longitude)
                                .toString()

                            edt_address.setText(coordinates)
                            txt_address.visibility = View.VISIBLE
                            txt_address.text = "Implement late with Google API"

                        }
                }
            }

            builder.setView(view)
            builder.setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
                .setPositiveButton("YES") { dialogInterface, _ ->
                    Toast.makeText(context, "Implement late", Toast.LENGTH_SHORT).show()
                }

            val dialog = builder.create()
            dialog.show()

        }
    }

    private fun sumCart() {
        cartDataSource!!.sumPrice(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Double> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: Double) {
                    txt_total_price!!.text = java.lang.StringBuilder("Total: $ ")
                        .append(t)
                }

                override fun onError(e: Throwable) {
                    if (e.message!!.contains("Query returned empty"))
                        Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        cartviewModel.onStop()
        compositeDisposable.clear()
        EventBus.getDefault().postSticky(HideFABCart(false))
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)

        if (fusedLocationProviderclient != null)
            fusedLocationProviderclient.removeLocationUpdates(locationCalback)
        super.onStop()

    }

    override fun onResume() {
        super.onResume()
        calculateTotalPrice()
        if (fusedLocationProviderclient != null)
            fusedLocationProviderclient.requestLocationUpdates(
                locationRequest, locationCalback,
                Looper.getMainLooper()
            )
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUpdateItemInCart(event: UpdateItemInCart) {
        if (event.cartItem != null) {
            recyclerViewState = recycler_cart!!.layoutManager!!.onSaveInstanceState()
            cartDataSource!!.updateCart(event.cartItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSubscribe(d: Disposable) {
                        calculateTotalPrice()
                        recycler_cart!!.layoutManager!!.onRestoreInstanceState(recyclerViewState)
                    }

                    override fun onSuccess(t: Int) {

                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, "[UPDATE CART]" + e.message, Toast.LENGTH_SHORT)
                            .show()
                    }

                })
        }
    }

    private fun calculateTotalPrice() {
        cartDataSource!!.sumPrice(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Double> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(price: Double) {
                    txt_total_price!!.text =
                        StringBuilder("Total: $").append(Common.formatPrice(price))
                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty"))
                        Toast.makeText(context, "[SUM CART]" + e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_settings).isVisible = false //Hide Setting menu when in Cart
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.cart_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_clear_cart) {
            cartDataSource!!.cleanCart(Common.currentUser!!.uid!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(t: Int) {
                        Toast.makeText(context, "Clear Cart Success", Toast.LENGTH_SHORT).show()
                        EventBus.getDefault().postSticky(CountCartEven(true))
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
                    }

                })
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}