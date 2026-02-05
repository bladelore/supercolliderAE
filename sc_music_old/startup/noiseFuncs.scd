(
~velvet = {|t, density, bias|

    var n = Dseries(0, 0, inf);

    var high = 1 - density;
    var low = density * 2;

    var nx1 = Diwhite(0, 1, inf);
    //bias
    var nx2 = (Dwhite(0, 1, inf) < (0.5 + (bias * 0.5))).if(1, -1);

    var out = Dswitch1([0, nx2], nx1 >= high);

	Demand.ar(t, 0, out);
};
)



