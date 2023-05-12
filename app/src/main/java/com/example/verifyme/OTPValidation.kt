package com.example.verifyme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.verifyme.databinding.ActivityOtpvalidationBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class OTPValidation : AppCompatActivity() {
    private var _bindind: ActivityOtpvalidationBinding? = null
    private val binding get() = _bindind!!
    private lateinit var auth: FirebaseAuth
    private lateinit var OTP: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNo: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_VerifyMe)
        _bindind = ActivityOtpvalidationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        OTP = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("token" )!!
        phoneNo = intent.getStringExtra("number")!!

        binding.textOTPno.text = "+91-"+phoneNo


        binding.submitBtn.setOnClickListener {
            val otpEntered = binding.otpEdittext.text.toString()

            if (otpEntered.isNotEmpty()){
                if (otpEntered.length == 6){
                    val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(OTP, otpEntered)

                    signInWithPhoneAuthCredential(credential)


                }else
                    Toast.makeText(this, "Please enter valid otp", Toast.LENGTH_SHORT).show()
            }else
                Toast.makeText(this, "Please enter otp", Toast.LENGTH_SHORT).show()

        }
        resendOTPbtnVisibility()

        binding.resendOTP.setOnClickListener{
            resendOTP()
            resendOTPbtnVisibility()
        }


    }

    private fun resendOTP(){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNo) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendOTPbtnVisibility(){
        binding.otpEdittext.setText("")
        binding.resendOTP.visibility = View.INVISIBLE
        binding.resendOTP.isEnabled = false

        Handler(Looper.getMainLooper()).postDelayed({
            binding.resendOTP.visibility = View.VISIBLE
            binding.resendOTP.isEnabled = true
        },60000)
    }
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.

            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.


            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.d("TAG", "onVarificationFailed${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                Log.d("TAG", "onVarificationFailed${e.toString()}")
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                Log.d("TAG", "onVarificationFailed${e.toString()}")
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            OTP = verificationId
            resendToken = token


        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    sendToMain()
                    Log.d("TAG", "signInWithCredential:success")

                    val user = task.result?.user
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    private fun sendToMain(){
        startActivity(Intent(this, UserProfile::class.java))
    }
}