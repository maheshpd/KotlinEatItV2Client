package com.createsapp.kotlineatitv2client.ui.view_order

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.createsapp.kotlineatitv2client.R
import com.createsapp.kotlineatitv2client.adapter.MyOrderAdapter
import com.createsapp.kotlineatitv2client.callback.ILoadOrderCallbackListener
import com.createsapp.kotlineatitv2client.common.Common
import com.createsapp.kotlineatitv2client.model.Order
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog
import java.util.*
import kotlin.collections.ArrayList

class ViewOrderFragment : Fragment(), ILoadOrderCallbackListener {

    private var viewOrderModel: ViewOrderModel? = null

    internal lateinit var dialog: AlertDialog

    internal lateinit var recycler_order: RecyclerView

    internal lateinit var listener: ILoadOrderCallbackListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOrderModel = ViewModelProviders.of(this).get(ViewOrderModel::class.java)
        val root = inflater.inflate(R.layout.fragment_view_orders, container, false)

        initViews(root)
        loadOrderFromFirebase()

        viewOrderModel!!.mutableLiveDataOrderList.observe(viewLifecycleOwner, Observer {
            Collections.reverse(it!!)
            val adapter = MyOrderAdapter(requireContext(), it)
            recycler_order.adapter = adapter
        })

        return root
    }

    private fun loadOrderFromFirebase() {
        dialog.show()
        val orderList = ArrayList<Order>()

        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
            .orderByChild("userId")
            .equalTo(Common.currentUser!!.uid)
            .limitToLast(100)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (orderSnapShot in snapshot.children) {
                        dialog.dismiss()
                        val order = orderSnapShot.getValue(Order::class.java)
                        order!!.orderNumber = orderSnapShot.key
                        orderList.add(order)
                    }
                    listener.onLoadOrderSuccess(orderList)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.onLoadOrderFailed(error.message)
                }

            })
    }

    private fun initViews(root: View?) {

        listener = this
        dialog = SpotsDialog.Builder().setContext(requireContext()).setCancelable(false).build()

        recycler_order = root!!.findViewById(R.id.recycler_order) as RecyclerView
        recycler_order.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(requireContext())
        recycler_order.layoutManager = layoutManager
        recycler_order.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                layoutManager.orientation
            )
        )
    }

    override fun onLoadOrderSuccess(orderList: List<Order>) {
        //Implement late
        viewOrderModel!!.setMutableLiveDataOrderList(orderList)
    }

    override fun onLoadOrderFailed(message: String) {
        dialog.dismiss()
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}