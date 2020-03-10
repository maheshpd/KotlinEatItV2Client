package com.createsapp.kotlineatitv2client.ui.comment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.createsapp.kotlineatitv2client.model.CommentModel

class CommentViewModel : ViewModel() {

    val mutableLiveDataCommentList: MutableLiveData<List<CommentModel>> = MutableLiveData()

    fun setCommentList(commentList: List<CommentModel>) {
        mutableLiveDataCommentList.value = commentList
    }
}