# shakefeedback

摇一摇出现悬浮按钮，点击进去能对当前页面涂鸦并写字。适合做对当前页面反馈问题的功能。

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FeedbackManager().feedback(this)

    }

}
