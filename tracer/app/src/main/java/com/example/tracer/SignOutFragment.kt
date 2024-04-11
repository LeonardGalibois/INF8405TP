package com.example.tracer

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth


class SignOutFragment : DialogFragment() {
    lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.sign_out_message))
            .setPositiveButton(getString(R.string.sign_out_ok), object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) { confirm() }
            })
            .setNegativeButton(getString(R.string.sign_out_cancel), object: DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) { cancel() }
            })
            .create()

    fun confirm()
    {
        firebaseAuth.signOut()
        val intent = Intent(getActivity(), LoginActivity::class.java)
        startActivity(intent)
    }

    fun cancel()
    {

    }
}