package com.example.rxandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.Serializable
import java.lang.reflect.Array

class MainActivity : AppCompatActivity() {
    private lateinit var mDisposable: Disposable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val observable: Observable<User> = getUsersObservable()
//        val observer : Observer<User> = getUserObserver()

        val observable: Observable<Serializable> = getUsersObservable_just()
        val observer : Observer<Serializable> = getUserObserver_just()

        observable.subscribeOn(Schedulers.io())//đăng kí thread mà observable phát ra dữ liệu trên đó
            .observeOn(AndroidSchedulers.mainThread())// đăng kí thread mà observer sẽ nhận dữ liệu
            .subscribe(observer) // observer kết nối vs observable

    }
    private fun getUserObserver() : Observer<User> {
        return object:Observer<User>{
        override fun onSubscribe(d: Disposable) {
            Log.e("Tagx","onSubscribe ")
            mDisposable = d
        }

            override fun onError(e: Throwable) {
                Log.e("Tagx","onError")
            }

            override fun onComplete() {
                Log.e("Tagx","onComplete")
            }

            override fun onNext(user: User) {
                Log.e("Tagx","onNext : $user")
                Log.e("Tagx","onNext thread: " + Thread.currentThread().name   )
            }
        }
    }

    private fun getUserObserver_just() : Observer<Serializable>{
        return object :Observer<Serializable>{
            override fun onSubscribe(d: Disposable) {
                Log.e("Tagx","onSubscribe ")
                mDisposable = d
            }

            override fun onError(e: Throwable) {
                Log.e("Tagx","onError")
            }

            override fun onComplete() {
                Log.e("Tagx","onComplete")
            }

            override fun onNext(serializable: Serializable) {
                Log.e("Tagx","onNext : $serializable")
                Log.e("Tagx","onNext thread: " + Thread.currentThread().name   )
                when(serializable){
                    is kotlin.Array<*> ->
                        for (user in serializable){
                            Log.e("Tagx","$user")
                        }
                    is String -> Log.e("Tagx","String Type: $serializable")
                    is Int -> Log.e("Tagx","Int Type: $serializable")
                }
            }
        }
    }

    private fun getUsersObservable_just() : Observable<Serializable>{
        val user1 = User(1, "Xung")
        val user2 = User(2, "Nhan")
        val userArray = arrayOf(user1,user2)
        val stringX = "String here"
        val IntX = 2204

        return Observable.just(userArray, stringX, IntX)
    }

    private fun getUsersObservable_fromArray() : Observable<User>{
        val user1 = User(1, "Xung")
        val user2 = User(2, "Nhan")
        val userArray = arrayOf(user1,user2)
        return Observable.fromArray(*userArray)
    }

    private fun getUsersObservable_create() : Observable<User>{
//        phat du lieu nay di
        val userList : List<User> = getUsersList()
        return Observable.create(ObservableOnSubscribe {
            emitter: ObservableEmitter<User> ->
            Log.e("Tagx","Observable thread: " + Thread.currentThread().name   )
            if (userList.isEmpty()){
                if (!emitter.isDisposed){
                     emitter.onError(Exception())
                }
            }
            for (user in userList){
                //chua bi huy ket noi
                if (!emitter.isDisposed){
                    emitter.onNext(user)
                }
            }
            //neu khong bi ngat ket noi => bao cong viec hoan thanh
            if (!emitter.isDisposed){
                emitter.onComplete()
            }
        })
    }

    //đại diện cho công việc mà Rx thực hiện
    private fun getUsersList() : List<User>{
        val list : MutableList<User> = ArrayList()
        list.add(User(1, "Xung"))
        list.add(User(2, "Minh"))
        list.add(User(3, "Phuc"))
        return list
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!mDisposable.isDisposed){//ngắt kết nối giữa observer và observable
            mDisposable.dispose()
            Log.e("Tagx","Disposed")
        }
    }
}