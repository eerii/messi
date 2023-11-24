useLogger(CustomEventLogger())

class CustomEventLogger() : BuildAdapter(), TaskExecutionListener {
    override fun beforeExecute(task: Task) {
        if (task.name == "run")
            println("[${task.name}]\n")
    }

    override fun afterExecute(task: Task, state: TaskState) { }

    override fun buildFinished(result: BuildResult) {
        if (result.failure != null) {
            println("\nBuild with errors".red)
        } else {
            println("\nBuild completed".green)
        }
    }

    val String.red: String get() { return "\u001b[31m$this\u001b[0m" }
    val String.green: String get() { return "\u001b[32m$this\u001b[0m" }
}
