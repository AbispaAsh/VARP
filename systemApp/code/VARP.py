import subprocess
import threading

flask_process = 0

def run_flask():
    global flask_process
    flask_process = subprocess.Popen(['python', 'programs/flask_program.py'])
    flask_process.wait()

def run_tkinter():
    subprocess.call(['pythonw','programs/tkinter_program.pyw'])
    #if exe:
    #subprocess.call(['programs/tkinter_program.exe'])

if __name__ == '__main__':
    flask_thread = threading.Thread(target=run_flask)
    flask_thread.start()

    run_tkinter()
    if flask_thread.is_alive():
        flask_process.terminate()
    
        
    
        
