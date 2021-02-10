package com.codingeggs.todolist

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codingeggs.todolist.databinding.ActivityMainBinding
import com.codingeggs.todolist.databinding.ItemTodoBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    val RC_SIGN_IN = 1000
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 로그인이 안 되있으면
        if (FirebaseAuth.getInstance().currentUser == null) {
            login()
        }




        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = TodoAdapter(
                emptyList(),
                onClickDeleteIcon = {
                    viewModel.deleteTodo(it)
                },
                onClickItem = {
                    viewModel.toggleTodo(it)
                })
        }

        // 추가기능 추가
        binding.addButton.setOnClickListener {
            val todo = Todo(binding.editText.text.toString())
            viewModel.addTodo(todo)
        }

        // 관찰 UI 업데이트
        viewModel.todoLiveData.observe(this, Observer {
            (binding.recyclerView.adapter as TodoAdapter).setData(it)
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)


            if (resultCode == Activity.RESULT_OK) {
                // 로그인 성공
                // Successfully signed in
                viewModel.fetchData()
                // ...
            } else {
                // 로그인 실패 
                // 로그인을 안한채로 뒤로가기 하면 잠깐 메인 화면이 보이는데
                // 이를 막으려면 일일이 코드를 짜야한다
                finish()
            }
        }
    }

    fun login() {
        val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
    }

    fun logout() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                login()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_log_out -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

data class Todo(
    val text: String,
    var isDone: Boolean = false, // false가 디폴트값
)


class TodoAdapter(
    private var dataSet: List<Todo>,
    val onClickDeleteIcon: (todo: Todo) -> Unit,
    val onClickItem: (todo: Todo) -> Unit
) :
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {
    class TodoViewHolder(val item_binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(item_binding.root) {
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_todo, viewGroup, false)
        return TodoViewHolder(ItemTodoBinding.bind(view))
    }

    override fun onBindViewHolder(viewHolder: TodoViewHolder, position: Int) {
        val todo = dataSet[position]
        viewHolder.item_binding.todoText.text = todo.text

        if (todo.isDone) {
            viewHolder.item_binding.todoText.apply {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                setTypeface(null, Typeface.ITALIC)
            }
        } else {
            viewHolder.item_binding.todoText.apply {
                paintFlags = 0
                setTypeface(null, Typeface.NORMAL)
            }
        }

        viewHolder.item_binding.deleteImageView.setOnClickListener {
            onClickDeleteIcon.invoke(todo)
        }

        viewHolder.item_binding.root.setOnClickListener {
            onClickItem.invoke(todo)
        }
    }

    override fun getItemCount() = dataSet.size

    // 데이터 갱신 함수
    fun setData(newData: List<Todo>) {
        dataSet = newData
        notifyDataSetChanged()
    }
}

class MainViewModel : ViewModel() {
    val db = Firebase.firestore

    val todoLiveData = MutableLiveData<List<Todo>>()

    private val data = arrayListOf<Todo>()  // 밖에서 수정을 막음 private

    init {
        fetchData()
    }

    fun fetchData() {
        db.collection("todos")
            .get()
            .addOnSuccessListener { result ->
                data.clear()
                for (document in result) {
                    val todo = Todo(
                        document.data["text"] as String,
                        document.data["isDone"] as Boolean,
                    )
                    data.add(todo)
                }
                todoLiveData.value = data
            }
    }

    // 완료기능
    fun toggleTodo(todo: Todo) {
        todo.isDone = !todo.isDone
        todoLiveData.value = data
    }

    // 추가기능
    fun addTodo(todo: Todo) {
        data.add(todo)
        todoLiveData.value = data
    }

    // 삭제기능
    fun deleteTodo(todo: Todo) {
        data.remove(todo)
        todoLiveData.value = data
    }

}