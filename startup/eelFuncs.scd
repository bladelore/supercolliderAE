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

    function schroeder(in, size, damp) local(sig, ap1, ap2, ap3, x1,x2,x3,x4,s1,s2,o1,o2,o3,o4,left,right, 
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