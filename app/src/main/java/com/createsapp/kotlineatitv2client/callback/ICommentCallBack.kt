package com.createsapp.kotlineatitv2client.callback

import com.createsapp.kotlineatitv2client.model.CommentModel

interface ICommentCallBack {
    fun onCommentLoadSuccess(commentList: List<CommentModel>)
    fun onCommentLoadFailed(message: String)
}