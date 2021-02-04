package com.codingeggs.todolist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codingeggs.todolist.databinding.ActivityMainBinding
import com.codingeggs.todolist.databinding.ItemTodoBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val data = arrayListOf<Todo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        data.add(Todo("숙제", false))
        data.add(Todo("청소", false))

        binding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.recyclerView.adapter = TodoAdapter(data,
        onClickDeleteIcon = {
            deleteTodo(it)
        })

        // 추가기능 추가
        binding.addButton.setOnClickListener {
            Log.d("test", "setOnClickListener")
            addTodo()
        }


    }

    // 추가기능
    private fun addTodo() {
        val todo = Todo(binding.editText.text.toString())
        data.add(todo)
        binding.recyclerView.adapter?.notifyDataSetChanged()    // recyclerview 갱신
    }

    // 삭제기능
    private fun deleteTodo(todo: Todo) {
        data.remove(todo)
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

}

data class Todo(
    val text: String,
    var isDone: Boolean = false, // false가 디폴트값
)


class TodoAdapter(
    private val dataSet: List<Todo>,
    val onClickDeleteIcon: (todo: Todo) -> Unit
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
        viewHolder.item_binding.deleteImageView.setOnClickListener {
            onClickDeleteIcon.invoke(todo)
        }
    }

    override fun getItemCount() = dataSet.size
}
