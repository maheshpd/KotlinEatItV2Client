package com.createsapp.kotlineatitv2client.ui.fooddetail

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.andremion.counterfab.CounterFab
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.createsapp.kotlineatitv2client.R
import com.createsapp.kotlineatitv2client.common.Common
import com.createsapp.kotlineatitv2client.model.CommentModel
import com.createsapp.kotlineatitv2client.model.FoodModel
import com.createsapp.kotlineatitv2client.ui.comment.CommentFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import dmax.dialog.SpotsDialog

class FoodDetailFragment : Fragment(), TextWatcher {

    private lateinit var foodDetailViewModel: FoodDetailViewModel

    private lateinit var addonBottomSheetDialog: BottomSheetDialog

    private var img_food: ImageView? = null
    private var btnCart: CounterFab? = null
    private var btnRating: FloatingActionButton? = null
    private var food_name: TextView? = null
    private var food_description: TextView? = null
    private var food_price: TextView? = null
    private var number_button: ElegantNumberButton? = null
    private var ratingBar: RatingBar? = null
    private var btnShowComment: Button? = null
    private var rdi_group_size: RadioGroup? = null
    private var chip_group_user_selected_addon: ChipGroup? = null
    private var img_add_on: ImageView? = null
    private var waitingDialog: android.app.AlertDialog? = null

    //Addon layout
    private var chip_group_Addon: ChipGroup? = null
    private var edt_search_addon: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodDetailViewModel =
            ViewModelProviders.of(this).get(FoodDetailViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_food_detail, container, false)
        initViews(root)

        foodDetailViewModel.getMutableLiveDataFood().observe(viewLifecycleOwner, Observer {
            displayInfo(it)
        })

        foodDetailViewModel.getMutableLiveDataComment().observe(this, Observer {
            submitRatingToFirebase(it)
        })
        return root
    }

    private fun submitRatingToFirebase(commentModel: CommentModel?) {
        waitingDialog!!.show()

        //First, we will submit to comment Ref
        FirebaseDatabase.getInstance()
            .getReference(Common.COMMENT_REF)
            .child(Common.foodSelected!!.id!!)
            .push()
            .setValue(commentModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    addRatingToFood(commentModel!!.ratingValue.toDouble())
                }
                waitingDialog!!.dismiss()
            }
    }

    private fun addRatingToFood(ratingValue: Double) {
        FirebaseDatabase.getInstance()
            .getReference(Common.CATEGORY_REF)  //Select category
            .child(Common.categorySelected!!.menu_id!!) //Select menu in category
            .child("foods") //Select foods array
            .child(Common.foodSelected!!.key!!) //Select key
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    waitingDialog!!.dismiss()
                    Toast.makeText(context!!, "" + p0.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val foodModel = dataSnapshot.getValue(FoodModel::class.java)
                        foodModel!!.key = Common.foodSelected!!.key
                        //Apply rating
                        val sumRating = foodModel.ratingValue.toDouble() + (ratingValue)
                        val ratingCount = foodModel.ratingCount + 1
                        val result = sumRating / ratingCount

                        val updateData = HashMap<String, Any>()
                        updateData["ratingValue"] = result
                        updateData["ratingCount"] = ratingCount

                        //Update data in variable
                        foodModel.ratingCount = ratingCount
                        foodModel.ratingValue = result

                        dataSnapshot.ref
                            .updateChildren(updateData)
                            .addOnCompleteListener { task ->
                                waitingDialog!!.dismiss()
                                if (task.isSuccessful) {
                                    Common.foodSelected = foodModel
                                    foodDetailViewModel.setFoodModel(foodModel)
                                    Toast.makeText(context!!, "Thank you", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                    } else
                        waitingDialog!!.dismiss()
                }

            })
    }

    private fun displayInfo(it: FoodModel?) {
        Glide.with(context!!).load(it!!.image).into(img_food!!)
        food_name!!.text = StringBuilder(it.name!!)
        food_description!!.text = StringBuilder(it.description!!)
        food_price!!.text = StringBuilder(it.price!!).toString()
        ratingBar!!.rating = it.ratingValue.toFloat()

        //Set Size
        for (sizeModel in it.size) {
            val radioButton = RadioButton(context)
            radioButton.setOnCheckedChangeListener { compoundButon, b ->
                if (b)
                    Common.foodSelected!!.userSelectedSize = sizeModel
                calculateTotalPrice()
            }

            var params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1.0f
            )
            radioButton.layoutParams = params
            radioButton.text = sizeModel.name
            radioButton.tag = sizeModel.price

            rdi_group_size!!.addView(radioButton)
        }

        //Default first radio button select
        if (rdi_group_size!!.childCount > 0) {
            val radioButton = rdi_group_size!!.getChildAt(0) as RadioButton
            radioButton.isChecked = true
        }
    }

    private fun calculateTotalPrice() {
        var totalPrice = Common.foodSelected!!.price!!.toDouble()
        var displayPrice = 0.0

        //Addon
        if (Common.foodSelected!!.userSelectedAddon != null && Common.foodSelected!!.userSelectedAddon!!.size > 0) {
            for (addonModel in Common.foodSelected!!.userSelectedAddon!!)
                totalPrice += addonModel.price!!.toDouble()
        }

        //Size
        totalPrice += Common.foodSelected!!.userSelectedSize!!.price!!.toDouble()

        displayPrice = totalPrice * number_button!!.number.toInt()
        displayPrice = Math.round(displayPrice * 100.0) / 100.0

        food_price!!.text =
            java.lang.StringBuilder("").append(Common.formatPrice(displayPrice)).toString()
    }

    private fun initViews(root: View?) {

        addonBottomSheetDialog = BottomSheetDialog(context!!, R.style.DialogStyle)
        val layout_user_selected_addon = layoutInflater.inflate(R.layout.layout_addon_display, null)
        waitingDialog = SpotsDialog.Builder().setContext(context!!).setCancelable(false).build()
        chip_group_Addon =
            layout_user_selected_addon.findViewById(R.id.chip_group_addon) as ChipGroup
        edt_search_addon = layout_user_selected_addon.findViewById(R.id.edt_search) as EditText
        addonBottomSheetDialog.setContentView(layout_user_selected_addon)

        addonBottomSheetDialog.setOnDismissListener { dialog ->
            displayUserSelectedAddOn()
            calculateTotalPrice()
        }

        btnCart = root!!.findViewById(R.id.btnCart) as CounterFab
        img_food = root.findViewById(R.id.img_food) as ImageView
        btnRating = root.findViewById(R.id.btn_rating) as FloatingActionButton
        food_name = root.findViewById(R.id.food_name) as TextView
        food_description = root.findViewById(R.id.food_description) as TextView
        food_price = root.findViewById(R.id.food_price) as TextView
        number_button = root.findViewById(R.id.number_button) as ElegantNumberButton
        ratingBar = root.findViewById(R.id.ratingBar) as RatingBar
        btnShowComment = root.findViewById(R.id.btnShowComment) as Button
        rdi_group_size = root.findViewById(R.id.rdi_group_size) as RadioGroup
        img_add_on = root.findViewById(R.id.img_add_addon) as ImageView
        chip_group_user_selected_addon =
            root.findViewById(R.id.chip_group_user_selected_addon) as ChipGroup


        //Event
        img_add_on!!.setOnClickListener {
            if (Common.foodSelected!!.addon != null) {
                displayAllAddon()
                addonBottomSheetDialog.show()
            }
        }


        btnRating!!.setOnClickListener {
            showRatingDialog()
        }

        btnShowComment!!.setOnClickListener {
            val commentFragment = CommentFragment.getInstance()
            commentFragment.show(activity!!.supportFragmentManager, "CommentFragment")
        }

    }

    private fun displayAllAddon() {
        if (Common.foodSelected!!.addon.size > 0) {
            chip_group_Addon!!.clearCheck()
            chip_group_Addon!!.removeAllViews()
            edt_search_addon!!.addTextChangedListener(this)

            for (addonModel in Common.foodSelected!!.addon) {

                val chip = layoutInflater.inflate(R.layout.layout_chip, null, false) as Chip
                chip.text = StringBuilder(addonModel.name!!).append("(+$").append(addonModel.price)
                    .append(")").toString()
                chip.setOnCheckedChangeListener { compoundButton, b ->
                    if (b) {
                        if (Common.foodSelected!!.userSelectedAddon == null)
                            Common.foodSelected!!.userSelectedAddon = ArrayList()
                        Common.foodSelected!!.userSelectedAddon!!.add(addonModel)
                    }
                }

                chip_group_Addon!!.addView(chip)

            }
        }
    }

    private fun displayUserSelectedAddOn() {
        if (Common.foodSelected!!.userSelectedAddon != null && Common.foodSelected!!.userSelectedAddon!!.size > 0) {
            chip_group_user_selected_addon!!.removeAllViews()
            for (addonModel in Common.foodSelected!!.userSelectedAddon!!) {
                val chip =
                    layoutInflater.inflate(R.layout.layout_chip_with_delete, null, false) as Chip
                chip.text =
                    StringBuilder(addonModel.name!!).append("(+$").append(addonModel.price)
                        .append(")").toString()
                chip.isClickable = false
                chip.setOnCloseIconClickListener { view ->
                    chip_group_user_selected_addon!!.removeView(view)
                    Common.foodSelected!!.userSelectedAddon!!.remove(addonModel)
                    calculateTotalPrice()
                }
                chip_group_user_selected_addon!!.addView(chip)
            }
        } else if (Common.foodSelected!!.userSelectedAddon!!.size == 0)
            chip_group_user_selected_addon!!.removeAllViews()
    }

    private fun showRatingDialog() {

        var builder = AlertDialog.Builder(context!!)
        builder.setTitle("Rating Food")
        builder.setMessage("Please fill information")

        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_rating_comment, null)
        val ratingBar = itemView.findViewById<RatingBar>(R.id.ratingBar2)
        val edt_comment = itemView.findViewById<EditText>(R.id.edt_comment)

        builder.setView(itemView)
        builder.setNegativeButton("CANCEL") { dialog: DialogInterface?, which: Int -> dialog!!.dismiss() }
        builder.setPositiveButton("OK") { dialog: DialogInterface?, which: Int ->

            val commentModel = CommentModel()
            commentModel.name = Common.currentUser!!.name
            commentModel.uid = Common.currentUser!!.uid
            commentModel.comment = edt_comment.text.toString()
            commentModel.ratingValue = ratingBar.rating
            val serverTimeStamp = HashMap<String, Any>()
            serverTimeStamp["timeStamp"] = ServerValue.TIMESTAMP
            commentModel.commentTimeStamp = (serverTimeStamp)

            foodDetailViewModel.setCommentModel(commentModel)
        }

        val dialog = builder.create()
        dialog.show()

    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
        chip_group_Addon!!.clearCheck()
        chip_group_Addon!!.removeAllViews()
        for (addonModel in Common.foodSelected!!.addon) {
            if (addonModel.name!!.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                val chip = layoutInflater.inflate(R.layout.layout_chip, null, false) as Chip
                chip.text = StringBuilder(addonModel.name!!).append("(+$").append(addonModel.price)
                    .append(")").toString()
                chip.setOnCheckedChangeListener { compoundButton, b ->
                    if (b) {
                        if (Common.foodSelected!!.userSelectedAddon == null)
                            Common.foodSelected!!.userSelectedAddon = ArrayList()
                        Common.foodSelected!!.userSelectedAddon!!.add(addonModel)
                    }
                }
                chip_group_Addon!!.addView(chip)
            }
        }
    }
}