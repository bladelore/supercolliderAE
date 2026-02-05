(
~genFuncs = "
    function delta(in) local(init, prev)
    (
        !init ? (prev = in; init = 1);
        d = x - prev;
        prev = x;
        d;
    );

    function change(in) local(init, prev)
    (
        !init ? (prev = in; init = 1);
        d = in - prev;
        prev = in;
        sign(d);
    );

    function latch(in, pass) local(init, held)
    (
        !init ? (held = in; init = 1);
        pass ? held = in;
        held;
    );

    function sah(in, trig, thresh) local(prev, output, init)
    (
        init != 1 ? (prev = 0; output = 0; init = 1);
        (prev <= thresh && trig > thresh) ? (output = in);
        prev = trig;
        output;
    );

    function scale(in, inlow, inhigh, outlow, outhigh, power)
    (
        inscale = (inhigh - inlow) != 0 ? (1.0 / (inhigh - inlow)) : 0;
        outdiff = outhigh - outlow;
        value = (in - inlow) * inscale;
        
        value = (value > 0.0) ? pow(value, power) : (value < 0.0) ? -pow(-value, power) : value;
        value = (value * outdiff) + outlow;

        value;
    );

    function linear_interp(x, y, a) ( x + a * (y-x));

    //alias
    function mix(x, y, a) (linear_interp(x, y, a));

    function mstosamps(ms)( srate * ms * 0.001 );

    function sampstoms(s)( 1000 * s / srate );

    function fixnan(x) ((x && x) ? x : 0);

    function DCBlock(in1) local(x1 y1)
    (
        y = in1 - x1 + y1 * 0.9997;
        x1 = in1;
        y1 = y;
        y;
    );

    function noise() (scale(rand(), 0, 1, -1, 1, 1));

    function wrap(x, low, high)local(range, result)
    (
        range = high - low;
        result = range != 0 ? (x - floor((x - low) / range) * range) : low;

        result;
    );

    function clip(x, low, high) local(result)
    (
        result = (x < low) ? low :
                (x > high) ? high :
                x;
        result;
    );

    function phasor(freq, reset) local(init, prev)
    (
        !init || reset ? (prev = 0; init = 1);

        phase = (freq / srate) + prev;
        phase = reset ? 0 : phase;
        phase = wrap(phase, 0, 1);
        
        prev = phase;
        phase;
    );

    function triangle(phase, p1)
    (
        phase = wrap(phase, 0., 1.);
        p1 = clip(p1, 0., 1.);
        out = (phase < p1) ?
            ((p1) ? phase/p1 : 0.)
        :
            ((p1==1.) ? phase : 1. - ((phase - p1) / (1. - p1)));

        out;
    );

    function slide(in, slideup, slidedown) local(init, prev)
    (
        !init ? (prev = in; init = 1);
        slideup = 1/max(slideup, 1);
        slidedown = 1/max(slidedown, 1);

        diff = in - prev;
        x = (in > prev) ? diff * slideup : diff * slidedown;
        x = x + prev;
        prev = x;
        x;
    );

    function lpf_op_simple(in, damp) local(init, prev, lpf)(
        lpf = linear_interp(in, prev, damp);
        prev = lpf;
        lpf;
    );

    function onepole(in, freq, topology) local(init, prev)
    (
        !init ? (prev = 0; init = 1);

        freq = abs(freq);
        freq = clip(freq, 0, srate/4);
        freq = freq * ($pi/srate);
        freq = tan(freq);
        freq = freq / (freq + 1);

        x = in - prev;
        x1 = x * freq;
        x2 = x1 + prev;
        prev = x1 + x2;
        
        lp = x2;
        hp = in - x2;
        ap = hp + x2;

        out = (topology == 0) ? lp : (topology == 1) ? hp : ap;
        out;
    );

    function t60(t)( pow(0.001, 1.0 / t));

    function allpass(in, g) local(init, prev)
    (
        !init ? (prev = 0; init = 1);
        x1 = (prev * g) + in;
        x2 = (x1 * -g) + prev;
        prev = x1;
        x2;
    );

    function biquad(v0, cutoff, Q, topology) local(init, ic1eq, ic2eq)
    (
        !init ? (ic1eq = 0; ic2eq = 0; init = 1;);
        g = tan($pi * cutoff/srate);
        k = 1/Q;

        a1 = 1/(1 + g*(g + k));
        a2 = g*a1;
        a3 = g*a2;

        v3 = v0 - ic2eq;
        v1 = a1 * ic1eq + a2*v3;
        v2 = ic2eq + a2*ic1eq + a3*v3;

        ic1eq = 2*v1 - ic1eq;
        ic2eq = 2*v2 - ic2eq;

        low = v2;
        band = v1;
        high = v0 - k*v1 - v2;
        notch = low + high;
        peak = low - high;
        all = low + high - k*band;
        ubp = band / k;
        bshelf = ubp + v0;

        out = 
            (topology == 0) ? (low) :
            (topology == 1) ? (band) :
            (topology == 2) ? (high) :
            (topology == 3) ? (notch) :
            (topology == 4) ? (peak) :
            (topology == 5) ? (all) :
            (topology == 6) ? (ubp) :
            bshelf;
        out;
    );

    function unit_arc(in, shape) local(shape_min, shape_max, curve, curve_a, curve_b, arc_a, arc_b)
    (
        in = clip(in, 0, 1);
        shape = clip(shape, 0, 1);
        shape_min = shape < 0.5;
        shape_max = !shape_min;
        curve = tan(scale(shape, 1, 0, 0, 0.5*$pi, 1));
        curve_a = shape_min ? curve : 1/curve;
        curve_b = shape_min ? 1/curve : curve;
        arc_a = scale(in, shape_max, shape_min, 1, 0, curve_a);
        arc_b = scale(arc_a, 0, 1, shape_min, shape_max, curve_b);
        arc_b;
    );

        function allpass_delay(ap_delay_in, gain, delay_samps, buf) local(init, ptr, max_delay, read_pos, frac, a, b, tap, sig, out)
    (
        !init ? (ptr = 0; init = 1;);
        max_delay = mstosamps(3000);

        read_pos = wrap(ptr - delay_samps, 0, max_delay);
        frac = read_pos - floor(read_pos);
        a = wrap(floor(read_pos), 0, max_delay);
        b = wrap(a + 1, 0, max_delay);
        tap = mix(buf[a|0], buf[b|0], frac);

        sig = (ap_delay_in - tap) * gain;
        out = tap + sig;

        buf[ptr] = sig;
        ptr = wrap(ptr + 1, 0, max_delay);

        out;
    );

    function fbcomb(comb_in, gain, delay_samps, damp, buf) local(init, ptr, read_pos, max_delay, frac, a, b, tap, out)
    (
        
        !init ? (ptr = 0; init = 1;);
        max_delay = mstosamps(3000);

        read_pos = wrap(ptr - delay_samps, 0, max_delay);
        frac = read_pos - floor(read_pos);
        a = wrap(floor(read_pos), 0, max_delay);
        b = wrap(a + 1, 0, max_delay);
        tap = mix(buf[a|0], buf[b|0], frac);

        tap = lpf_op_simple(tap, damp);

        out = (comb_in - tap) * gain;

        buf[ptr] = out;
        ptr = wrap(ptr+1, 0, max_delay);

        out;
    );

    function fuck_verb(in, size, damp) local(sig, ap1, ap2, ap3, x1,x2,x3,x4,s1,s2,o1,o2,o3,o4,left,right, 
    ap1_buf, ap2_buf, ap3_buf, x1_buf, x2_buf, x3_buf, x4_buf)
    (
        ap1_buf[0]+=0;
        ap2_buf[0]+=0;
        ap3_buf[0]+=0;

        sig = lpf_op_simple(in, damp);
        ap1 = allpass_delay(sig, 0.7, 347 * size, ap1_buf);
        ap2 = allpass_delay(ap1, 0.7, 113 * size, ap2_buf);
        // ap3 = allpass_delay(ap2, 0.7, 370 * size, ap3_buf);
        ap3 = ap2;

        x1_buf[0]+=0;
        x2_buf[0]+=0;
        x3_buf[0]+=0;
        x4_buf[0]+=0;

        x1 = fbcomb(ap3, 0.773, 1687*size, damp, x1_buf);
        x2 = fbcomb(ap3, 0.802, 1601*size, damp, x2_buf);
        x3 = fbcomb(ap3, 0.753, 2053*size, damp, x3_buf);
        x4 = fbcomb(ap3, 0.733, 2251*size, damp, x4_buf);

        s1 = x1 + x3;
        s2 = x2 + x4;
        o1 = s1 + s2;
        o2 = ((s1 + s2));
        o3 = ((s1 - s2));
        o4 = s1 - s2;

        left=o1+o3;
        right=o2+o4;

        // left=0;
        // right=0;
        left+right;
        // (in*drywet) + ( (left+right) *(drywet) );
    );

";
)

//subharmonic string
(
~genDef = DynGenDef(\fmString, 
    ~genFuncs ++
    "
    function pluck(in, freq) local(init, prev)
    (
        !init ? (prev = 0; init = 1);
        x = slide(in, 1, 100) * noise();
        coeff = exp(abs(freq) * (-2*$pi) / srate);
        x = linear_interp(x, prev, coeff);
        prev = x;
        x;
    );
    
    function string_resonator(in, freq, fb, filter) local(init, del_ptr, prev1, prev2)(
        !init ? (
            prev1 = 0;
            prev2 = 0;
            del_ptr = 0;
            init = 1
        );

        freq = srate/abs(freq);
        coeff = exp(abs(filter) * (-2*$pi) / srate);
        delay_samp = (2*log(1-coeff)) + freq;

        lpf = linear_interp(prev1, prev2, 1-coeff);
        prev1 = lpf;

        delay[del_ptr] = DCBlock(in + lpf);
        read_pos = wrap(del_ptr - delay_samp, 0, srate);
        delay_rd = delay[read_pos];

        prev2 = delay_rd * fb;
        del_ptr = wrap(del_ptr + 1, 0, srate);

        delay_rd;
    );

    pitch_hz = in1;
    fb = in2;
    filter = in3;
    fuzz = in4;
    subharmonic = in5;
    exciter_filter = in6;

    impulse = pluck(in0, exciter_filter);
    subharmonic_hz = pitch_hz / subharmonic;
    mod = sin(phasor(subharmonic_hz, 0) * 2*$pi) * (fuzz * subharmonic_hz);
    out0 = string_resonator(impulse, pitch_hz + mod, fb, filter);
    "
).send;

SynthDef(\fmString, {|gate=1|
    var sig = DynGenRT.ar(1, \fmString, 
        Impulse.ar(0) ! 2,
        \midipitch.ar(42.midicps()),
        \fb.ar(-1),
        \filter.ar(2000),
        \fuzz.ar(0.01),
        \subharmonic.ar(1),
        \exciter_filter.ar(200)
    );
    
    sig = sig * EnvGen.ar(Env.perc(\atk.kr(0), \rel.kr(2), level: 1, curve: -2), gate, doneAction: 2);
    sig = sig * \amp.kr(1);
    sig = sig * \gain.kr(1).dbamp;

    Out.ar(\out.kr(0), sig.sanitize);
}).add;
);

Synth(\fmString)
//death string
(
~genDef = DynGenDef(\funcs, 
    ~genFuncs ++ 
    "
    function pluck(in, freq) local(init, prev)
    (
        !init ? (prev = 0; init = 1);
        x = slide(in, 1, 100) * noise();
        coeff = exp(abs(freq) * (-2*$pi) / srate);
        x = linear_interp(x, prev, coeff);
        prev = x;
        x;
    );
    
    function string_resonator(in, freq, fb, damp, filter, q, topology, shape, verb_size, verb_mix) 
    local(init, del_ptr, prev, freq, damping_offset, delay_fract, delay_samp, ap_coeff, delay, read_pos, delay_rd, shaper, biquad, ap, lpf, out)
    (
        !init ? (prev = 0; del_ptr = 0; init = 1);

        freq = srate/abs(freq);
        damping_offset = (2*log(damp)) + freq;

        delay_fract = wrap(damping_offset, 0.3, 1.3);
        delay_samp = damping_offset - delay_fract;
        ap_coeff = (delay_fract - 1) / (delay_fract + 1);

        delay[del_ptr] = DCBlock(in + (prev * fb));
        read_pos = wrap(del_ptr - delay_samp, 0, srate);
        delay_rd = delay[read_pos];

        verb = fuck_verb(delay_rd, verb_size, 0.5);
        mixed = linear_interp(delay_rd, verb, -1);
        
        delay_rd = DCBlock(scale(unit_arc(mixed, shape), 0, 1, -1, 1, 1));

        // shaper = DCBlock(unit_arc(delay_rd, shape));

        biquad = biquad(delay_rd, filter, q, topology);

        ap = allpass(biquad, ap_coeff);
        damp = lpf_op_simple(ap, 1-damp);
        prev = damp;
        out = damp;
        
        del_ptr = wrap(del_ptr + 1, 0, srate);

        out;
    );

    pitch_hz = in1; fb = in2; damp = in3; exciter_filter = in4;
    
    fuzz = in5; subharmonic = in6;
    
    filter = in7; q = in8; topology = in9;

    shaper = in10;

    verb_size = in11; verb_mix = in12;

    impulse = pluck(in0, exciter_filter);
    subharmonic_hz = pitch_hz / subharmonic;
    mod = sin(phasor(subharmonic_hz, 0) * 2*$pi) * (fuzz * subharmonic_hz);
    out0 = string_resonator(impulse, pitch_hz + mod, fb, damp, filter, q, topology, shaper, verb_size, verb_mix);
    "
).send;

Ndef(\test).clear;
Ndef(\test).play;

Ndef(\test, {
    var sig = DynGenRT.ar(1, ~genDef, 
        Impulse.ar(100) ! 2,

        \midipitch.ar(20.midicps()),
        \fb.ar(1),
        \damp.ar(0.5),
        \exciter_filter.ar(2000),

        \fuzz.ar(1),
        \subharmonic.ar(1),

        \filter.ar(500),
        // (topology == 0) ? (low) :
        // (topology == 1) ? (band) :
        // (topology == 2) ? (high) :
        // (topology == 3) ? (notch) :
        // (topology == 4) ? (peak) :
        // (topology == 5) ? (all) :
        // (topology == 6) ? (ubp) :
        // SinOsc.ar(0.1).exprange(100, 2000),
        \q.ar(1),
        \topology.ar(2),
        \shaper.ar(0.2),
        \verb_size.ar(5),
        // SinOsc.ar(0.1).range(5,100),
        \verb_mix.ar(0)
        // SinOsc.ar(0.1).range(0,1)
    ).sanitize;
    sig;
}).set(\out, ~bus1).gui;
);

(
~genDef = DynGenDef(\funcs, 
    ~genFuncs ++ 
    "
        
        function read_lin(buf, pos, max)
        (
            a = pos | 0;
            b = wrap(a + 1, 0, max);
            frac = pos - a;

            buf[a]*(1-frac) + buf[b]*frac;
        );


        function fdn_read(buf, ptr, delay_samps, max)
        (
            read_pos = wrap(ptr - delay_samps, 0, max);
            read_lin(buf, read_pos, max);
            buf[read_pos]
        );

        // FDN step
        function fdn_step(sig, gain, size)
        (

            MAX_SIZE=srate;
            buf0=MAX_SIZE*1;
            buf1=MAX_SIZE*2;
            buf2=MAX_SIZE*3;
            buf3=MAX_SIZE*4;

            max_delay = MAX_SIZE;

            d0_len = 149*size;
            d1_len = 211*size;
            d2_len = 263*size;
            d3_len = 347*size;

            // read old samples with linear interpolation
            d0 = fdn_read(buf0, ptr0, d0_len, max_delay);
            d1 = fdn_read(buf1, ptr1, d1_len, max_delay);
            d2 = fdn_read(buf2, ptr2, d2_len, max_delay);
            d3 = fdn_read(buf3, ptr3, d3_len, max_delay);

            // Hadamard feedback
            fb0 = (d0 + d1 + d2 + d3) * gain + sig;
            fb1 = (d0 - d1 + d2 - d3) * gain + sig;
            fb2 = (d0 + d1 - d2 - d3) * gain + sig;
            fb3 = (d0 - d1 - d2 + d3) * gain + sig;

            // write back
            buf0[ptr0] = fb0;
            buf1[ptr1] = fb1;
            buf2[ptr2] = fb2;
            buf3[ptr3] = fb3;

            ptr0 = wrap(ptr0 + 1, 0, max_delay);
            ptr1 = wrap(ptr1 + 1, 0, max_delay);
            ptr2 = wrap(ptr2 + 1, 0, max_delay);
            ptr3 = wrap(ptr3 + 1, 0, max_delay);

            s1 = fb0 + fb2;
            s2 = fb1 + fb3;
            o1 = s1 + s2;
            o2 = ((s1 + s2) * -1);
            o3 = ((s1 - s2) * -1);
            o4 = s1 - s2;

            out0=o1+o3;
            out1=o2+o4;
        );


        out0 = fdn_step(in0, 0.499, 5);
    "
).send;

Ndef(\test).clear;
Ndef(\test).play;
Ndef(\test, {
    var test = Decay.ar(Impulse.ar(1), 0.25, LFCub.ar(1200, 0, 0.1));
    var sig = DynGen.ar(2, ~genDef, test, 1, 0.09, 0.1).sanitize;

    sig.poll;

    sig;
}).set(\out, ~bus1);
);


//schroeder test
(
~genDef = DynGenDef(\funcs, 
        "
        function wrap(x, low, high)local(range, result)
        (
            range = high - low;
            result = range != 0 ? (x - floor((x - low) / range) * range) : low;

            result;
        );

        function mstosamps(ms)( srate * ms * 0.001 );

        function linear_interp(x, y, a) ( x + a * (y-x));

        function lpf_op_simple(in, damp) local(init, prev, lpf)(
            lpf = linear_interp(in, prev, damp);
            prev = lpf;
            lpf;
        );

        function allpass_delay(in, gain, delay_samps, buf) local(init, ptr, max_delay, read_pos, frac, a, b, tap, sig, out)
        (
            !init ? (ptr = 0; init = 1;);
            max_delay = srate;

            read_pos = wrap(ptr - delay_samps, 0, max_delay);
            frac = read_pos - floor(read_pos);
            a = wrap(floor(read_pos), 0, max_delay);
            b = wrap(a + 1, 0, max_delay);
            tap = linear_interp(buf[a|0], buf[b|0], frac);

            sig = (in - tap) * gain;
            out = tap + sig;

            buf[ptr] = sig;
            ptr = wrap(ptr + 1, 0, max_delay);

            out;
        );

        function fbcomb(in, gain, delay_samps, damp, buf) local(init, ptr, read_pos, max_delay, frac, a, b, tap, out)
        (
            
            !init ? (ptr = 0; init = 1;);
            max_delay = srate;

            read_pos = wrap(ptr - delay_samps, 0, max_delay);
            frac = read_pos - floor(read_pos);
            a = wrap(floor(read_pos), 0, max_delay);
            b = wrap(a + 1, 0, max_delay);
            tap = linear_interp(buf[a|0], buf[b|0], frac);

            // tap = lpf_op_simple(tap, damp);

            out = (in - tap) * gain;

            buf[ptr] = out;
            ptr = wrap(ptr+1, 0, max_delay);

            out;
        );
        
        size=in1; damp=in2;

        MAX_SIZE=srate;
        ap1_buf=MAX_SIZE*1;
        ap2_buf=MAX_SIZE*2;
        ap3_buf=MAX_SIZE*3;

        sig = lpf_op_simple(in0, damp);
        ap1 = allpass_delay(sig, 0.7, 347 * size, ap1_buf);
        ap2 = allpass_delay(ap1, 0.7, 113 * size, ap2_buf);
        ap3 = allpass_delay(ap2, 0.7, 37 * size, ap3_buf);
        
        sig = ap3;

        x1_buf=MAX_SIZE*4;
        x2_buf=MAX_SIZE*5;
        x3_buf=MAX_SIZE*6;
        x4_buf=MAX_SIZE*7;

        x1 = fbcomb(sig, 0.773, 1687, damp, x1_buf);
        x2 = fbcomb(sig, 0.802, 1601, damp, x2_buf);
        x3 = fbcomb(sig, 0.753, 2053, damp, x3_buf);
        x4 = fbcomb(sig, 0.733, 2251, damp, x4_buf);

        s1 = x1 + x3;
        s2 = x2 + x4;
        o1 = s1 + s2;
        o2 = ((s1 + s2) * -1);
        o3 = ((s1 - s2) * -1);
        o4 = s1 - s2;

        left=o1+o3;
        right=o2+o4;

        out0=left;
        out1=right;
    "
).send;

Ndef(\schroeder).clear;
Ndef(\schroeder, {
    var test = Decay.ar(Impulse.ar(1), 0.25, LFCub.ar(1200, 0, 0.1));
    var sig = DynGen.ar(2, ~genDef, test, 100, 0).sanitize;
    sig;

});

Ndef(\schroeder).play;

);