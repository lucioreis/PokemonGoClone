package com.trabalhopratico.grupo.pokemongoclone.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Interpolator;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.trabalhopratico.grupo.pokemongoclone.R;
import com.trabalhopratico.grupo.pokemongoclone.model.Aparecimento;
import com.trabalhopratico.grupo.pokemongoclone.util.CameraPreview;
//TODO - acrescentar sons
//TODO - colocar as duas imageviews(pokemon e
public class CapturarActivity extends Activity implements SensorEventListener, View.OnTouchListener{
    private ImageView _pokeball, pokemon, explosion;
    private  Aparecimento aparecimento;
    private Intent it;
    private Sensor sensor;
    private SensorManager sensorManager;
    private float grausNovos[] = new float[3];
    private float grausTotais[] = new float[3];
    private float grausVelhos[] = new float[3];
    private Display dispaly;
    private Double coeficiente;
    private int larguraDaTela, alturaDaTela;
    private float dx, dy,centerX, centerY, pokeballX, pokeballY;
    private float density;
    CameraPreview cameraSurfaceView;
    boolean capiturou = false;
    boolean  a = false, s = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capturar);
        cameraSurfaceView = new CameraPreview(this);
        it = getIntent();
        aparecimento = (Aparecimento) it.getSerializableExtra("Apar");
        _pokeball = (ImageView) findViewById(R.id.pokeball_image_view);
        _pokeball.setImageResource(R.drawable.pokeball);
        _pokeball.setOnTouchListener(this);
        pokemon = (ImageView) findViewById(R.id.pokemon);
        pokemon.setImageResource(aparecimento.getPokemon().getFoto());

        Log.i("captura", getResources().getResourceEntryName( aparecimento.getPokemon().getFoto()));
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(sensor == null) Toast.makeText(this, "Nao tem girsocopiculos", Toast.LENGTH_SHORT).show();
        else {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
        density = getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onResume(){
        super.onResume();
        Display display = getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        alturaDaTela = size.y;
        larguraDaTela = size.x;
        cameraSurfaceView.safeCameraOpenInView(findViewById(R.id.camera_surface_view));

        pokemon.post(new Runnable() {
            @Override
            public void run() {

                /*alturaDaTela = getResources().getDisplayMetrics().heightPixels;
                larguraDaTela = getResources().getDisplayMetrics().widthPixels;*/
                Log.i("Dimensao", "A: " + alturaDaTela + " L: " + larguraDaTela);

                coeficiente = larguraDaTela/72d;
                float larguraImgPkm = larguraDaTela/2;
                float proporcao = (pokemon.getMeasuredWidth())/larguraDaTela;
                float alturaImgPkm = pokemon.getHeight()*proporcao/2;
                centerX = larguraDaTela/2 - (((int) larguraImgPkm)/2);
                centerY = alturaDaTela/2  - (((int) alturaImgPkm)/2);
                pokemon.getLayoutParams().height = (int) alturaImgPkm;
                pokemon.getLayoutParams().width = (int) larguraImgPkm;
               // pokemon.setX(centerX);// - pokemon.getWidth()/2);
               // pokemon.setY(centerY);

                Log.i("Dimensao", "Posicao pkmn - X: " + pokemon.getX() + " Y: " + pokemon.getY());
                a = true;
            }
            });
        _pokeball.post(new Runnable() {
            @Override
            public void run() {
                _pokeball.getLayoutParams().height = (int)(larguraDaTela*(float)0.15);
                _pokeball.getLayoutParams().width = (int)(larguraDaTela*(float)0.15);
                Log.i("Dimensao", "MW: " + _pokeball.getMeasuredWidth() + " MH: " + _pokeball.getMeasuredHeight());
                //_pokeball.setY(pokeballY);
                //_pokeball.setX(pokeballX);
                s = true;
                Log.d("captura", pokemon.getX()+" "+ pokemon.getY() +  ' ' + pokemon.getLeft() + " " + pokemon.getRight());
                Log.d("captura", "X x Y = " + _pokeball.getMeasuredHeight()+ " + " + _pokeball.getMeasuredWidth());

                Log.i("Dimensao", "Posicao pokebola - X: " + _pokeball.getX() + " Y: " + _pokeball.getY());
            }
        });


    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * Called when there is a new sensor event.  Note that "on changed"
     * is somewhat of a misnomer, as this will also be called if we have a
     * new reading from a sensor with the exact same sensor values (but a
     * newer timestamp).
     * <p>
     * <p>See {@link SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link SensorEvent SensorEvent}.
     * <p>
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link SensorEvent SensorEvent}.
     */
    private float x = 1, y = 1;
    @Override
    public void onSensorChanged(SensorEvent event) {
        //if(!(a && s)) return;
        //TODO - Testar o giroscópio melhor
        float xyz[] = new float[3];
        for(int i = 0; i < 3; i++){
            xyz[i] = (float) (event.values[i]*59.2958);
            grausNovos[i] = (float) (xyz[i]*0.02);
            grausTotais[i] += grausNovos[i];
            if( !(grausTotais[i] > grausVelhos[i] + 0.08 || grausTotais[i] < grausVelhos[i] - 0.08) )
                grausTotais[i] = grausVelhos[i];
            grausVelhos[i] = grausTotais[i];
        }
        if(grausTotais[1] > 180){
            grausTotais[1] = grausTotais[1] - 360;
        }
        if(grausTotais[1] < -180){
            grausTotais[1] = 360 + grausTotais[1];
        }
        if(grausTotais[0] > 180){
            grausTotais[0] = grausTotais[0] - 360;
        }
        if(grausTotais[0] < -180){
            grausTotais[0] = 360 + grausTotais[0];
        }
        //Log.i("captura", "gtx = "+ grausTotais[1] + " gty= "+grausTotais[0]);
        if(pokemon.hasWindowFocus()) {
//            pokemon.setX((float) (grausTotais[1] * coeficiente));
//            pokemon.setY((float) (grausTotais[0] * coeficiente));
            pokemon.setRotation(grausTotais[2]);
            float d = (float) (grausTotais[1]*coeficiente);
            float e = (float) (grausTotais[0]*coeficiente);
            pokemon.animate()
                    .x(d)
                    .y(e)
                    .setDuration(0)
                    .start();
        }
        //Log.d("captura", pokemon.getTop()+" "+pokemon.getBottom() +  ' ' + pokemon.getLeft() + " " + pokemon.getRight());

    }

    /**
     * Called when the accuracy of the registered sensor has changed.  Unlike
     * onSensorChanged(), this is only called when this accuracy value changes.
     * <p>
     * <p>See the SENSOR_STATUS_* constants in
     * {@link SensorManager SensorManager} for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    long timer_inicio, timer_fim;
    float x0, y0, xf, yf;
    @Override
    public boolean onTouch(View v, final MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                timer_inicio = System.currentTimeMillis();
                x0 = event.getRawX() - (v.getWidth() / 2);
                y0 = event.getRawY() - (v.getHeight() / 2);
                v.setX(x0); v.setY(y0);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                timer_fim = System.currentTimeMillis();
                final long tempo = (timer_fim - timer_inicio);
                xf = v.getX(); yf = v.getY();
                animacaoDeLanamento(tempo, v);
                break;
            case MotionEvent.ACTION_MOVE:
                v.animate()
                        .x(event.getRawX() - v.getWidth()/2)
                        .y(event.getRawY() - v.getHeight()/2)
                        .setDuration(0)
                        .start();
                break;
            default:
                return false;
        }
        return true;
    }

    public void animacaoDeLanamento(Long tempo, View v){
        Double velocidadeX = (double)(xf - x0)/(tempo);
        Double velocidadeY = (double)(yf - y0)/(tempo);
//                velocidadeX = velocidadeX*(50000/larguraDaTela);
//                velocidadeY = velocidadeY*(50000/alturaDaTela);
        int direcaoX = (velocidadeX <= 0)? -1:1;
        int direcaoY = (velocidadeY <= 0)? -1:1;

        final ValueAnimator objectAnimatorX = ObjectAnimator.ofFloat(_pokeball, "translationX",
                v.getX(), direcaoX*larguraDaTela);
        Long tempoDeTransicaoX = ((larguraDaTela/velocidadeX) < 50) ? 50 : (long) Math.abs(larguraDaTela/velocidadeX);
        objectAnimatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(checaColisao(pokemon, _pokeball)){
                    Log.d("colisaoX", "colidiu");
                    _pokeball.setVisibility(View.INVISIBLE);
                    capiturou = capituraPokemon();
                }else Log.d("colisaoX", "nao colidiu");

                float f = (float) animation.getAnimatedValue();
                _pokeball.setTranslationX(f);
            }

        });
        objectAnimatorX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                _pokeball.setX(larguraDaTela/2 - _pokeball.getWidth()/2);
                _pokeball.setY(alturaDaTela - _pokeball.getHeight() - 50);
                _pokeball.setVisibility(View.VISIBLE);
            }
        });
        final ValueAnimator objectAnimatorY = ObjectAnimator.ofFloat(_pokeball, "translationY",
                v.getY(), direcaoY*alturaDaTela);
        Long tempoDeTransicaoY = ((larguraDaTela/velocidadeY) < 50) ? 50 : (long) Math.abs(larguraDaTela/velocidadeX);
        objectAnimatorY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(checaColisao(pokemon, _pokeball)){
                    Log.d("colisaoY", "colidiu");
                    capiturou = capituraPokemon();
                }else Log.d("colisaoY", "nao colidiu");

                float f = (float) animation.getAnimatedValue();
                _pokeball.setTranslationY(f);

            }

        });
        objectAnimatorY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                _pokeball.setX(larguraDaTela/2 - _pokeball.getWidth()/2);
                _pokeball.setY(alturaDaTela - _pokeball.getHeight() - 50);
                _pokeball.setVisibility(View.VISIBLE);
            }
        });
        Log.d("velocidade", "vel= "+velocidadeX+" tempTX= "+tempoDeTransicaoX+" tempTY= "+tempoDeTransicaoY+" tempo(s)= "+tempo/1000+" larguraDaTela= "+larguraDaTela);
        objectAnimatorX.setInterpolator(new LinearInterpolator());
        objectAnimatorY.setInterpolator(new LinearInterpolator());
        objectAnimatorX.setDuration(tempoDeTransicaoX);
        objectAnimatorY.setDuration(tempoDeTransicaoY);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(objectAnimatorX).with(objectAnimatorY);
        animatorSet.start();
        //TODO - Parar a pokebola e retorna-la para a posição de começo
        if(!capiturou)
            Toast.makeText(getBaseContext(), "Errou a pokebola", Toast.LENGTH_SHORT).show();
    }

    private boolean capituraPokemon() {
        //TODO-Terminar aniamção de captura
        pokemon.setVisibility(View.INVISIBLE);
        _pokeball.setX(pokemon.getX() - pokemon.getWidth() / 2);
        _pokeball.setY(pokemon.getY() - pokemon.getHeight() / 2);
        explosion = (ImageView) findViewById(R.id.explosao);
        explosion.setImageResource(R.drawable.explosion);
        final Handler handler = new Handler();
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                explosion.setVisibility(View.INVISIBLE);
            }
        };
        explosion.animate().setDuration(350).start();
        explosion.postDelayed(task, 350);
        final Runnable task2 = new Runnable() {
            @Override
            public void run() {
                AnimationSet animatorSet = new AnimationSet(true);
                RotateAnimation rotateAnimation = new RotateAnimation(340, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setDuration(350);
                RotateAnimation rotateAnimation1 = new RotateAnimation(0, 20, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation1.setDuration(700);
                RotateAnimation rotateAnimation2 = new RotateAnimation(20, -20, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation1.setDuration(350);
                animatorSet.setInterpolator(new LinearInterpolator());
                animatorSet.addAnimation(rotateAnimation1);
                animatorSet.addAnimation(rotateAnimation2);
                animatorSet.addAnimation(rotateAnimation1);
                _pokeball.startAnimation(animatorSet);
            }
        };
        _pokeball.postDelayed(task2, 351);
        _pokeball.postDelayed(task2, 1751);
        //TODO - Persistir pokemon no banco de dados
        return true;
    }

    public boolean checaColisao(View pokemon,View pokebola) {
        int pokemonTop = (int) pokemon.getY();
        int pokemonLeft = (int) pokemon.getX();
        int pokemonBottom = pokemonTop + pokemon.getHeight();
        int pokemonRight = pokemonLeft + pokemon.getWidth();

        int pokebolaTop = (int) pokebola.getY();
        int pokebolaLeft = (int) pokebola.getX();
        int pokebolaBottom = pokebolaTop + pokebola.getHeight();
        int pokebolaRight = pokebolaLeft + pokebola.getWidth();

        Log.d("coli_pokemon",pokemon.getLeft()+" "+ pokemon.getTop()+" "+ pokemon.getRight()+" "+ pokemon.getBottom());
        Rect R1_pokemon = new Rect(pokemonLeft, pokemonTop, pokemonRight, pokemonBottom);
        Rect R2_pokebola = new Rect(pokebolaLeft, pokebolaRight, pokebolaRight,pokebolaBottom);
        return R1_pokemon.contains(R2_pokebola);
    }
};
