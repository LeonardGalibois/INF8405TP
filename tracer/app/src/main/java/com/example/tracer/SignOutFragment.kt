package com.example.tracer

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment


class SignOutFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.sign_out_message))
            .setPositiveButton(getString(R.string.sign_out_ok)) { dialog, which -> confirm() }
            .setNegativeButton(getString(R.string.sign_out_cancel)) { dialog, which -> cancel() }
            .create()

    fun confirm()
    {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
    }

    fun cancel()
    {

    }
}