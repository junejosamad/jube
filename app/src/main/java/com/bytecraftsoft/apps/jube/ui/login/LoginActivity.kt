package com.bytecraftsoft.apps.jube.ui.login

import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.bytecraftsoft.apps.jube.databinding.ActivityLoginBinding
import com.bytecraftsoft.apps.jube.R
import com.bytecraftsoft.apps.jube.auth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory()).get(LoginViewModel::class.java)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        loginViewModel.loginFormState.observe(this@LoginActivity, Observer { loginState ->
            loginState ?: return@Observer

            // Disable login button unless both username and password are valid
            binding.login.isEnabled = loginState.isDataValid

            loginState.usernameError?.let {
                binding.username.error = getString(it)
            }
            loginState.passwordError?.let {
                binding.password.error = getString(it)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer { loginResult ->
            loginResult ?: return@Observer

            binding.loading.visibility = View.GONE

            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
                authenticateUser(binding.username.text.toString(), binding.password.text.toString())
            }
        })
    }

    private fun setupListeners() {
        binding.username.afterTextChanged {
            loginViewModel.loginDataChanged(
                binding.username.text.toString(),
                binding.password.text.toString()
            )
        }

        binding.password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    binding.username.text.toString(),
                    binding.password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    binding.loading.visibility = View.VISIBLE
                    loginViewModel.login(
                        binding.username.text.toString(),
                        binding.password.text.toString()
                    )
                }
                false
            }

            binding.login.setOnClickListener {
                binding.loading.visibility = View.VISIBLE
                loginViewModel.login(
                    binding.username.text.toString(),
                    binding.password.text.toString()
                )
            }
        }
    }

    private fun authenticateUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    Toast.makeText(this, "Authentication Successful", Toast.LENGTH_SHORT).show()
                    updateUI(user)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = auth.currentUser?.displayName
        // TODO: Initiate successful logged in experience
        Toast.makeText(applicationContext, "$welcome $displayName", Toast.LENGTH_LONG).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun updateUI(user: FirebaseUser?) {
        // TODO: Implement this method to update UI after login
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
