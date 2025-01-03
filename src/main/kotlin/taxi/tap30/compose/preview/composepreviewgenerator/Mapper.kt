package taxi.tap30.compose.preview.composepreviewgenerator


 fun getBooleanValue(type: BooleanInputType?): Boolean {
    return when (type) {
        BooleanInputType.TRUE -> true
        BooleanInputType.FALSE -> false
        else -> true
    }

}

 fun getStringValue(type: StringInputType?): String {
    return when (type) {
        StringInputType.short -> "\"متن ساختگی\""
        StringInputType.medium -> "\"لورم ایپسوم متن ساختگی با تولید سادگی نامفهوم از صنعت چاپ\""
        StringInputType.long -> "\"لورم ایپسوم متن ساختگی با تولید سادگی نامفهوم از صنعت چاپ، و با استفاده از طراحان گرافیک است، چاپگرها و متون بلکه روزنامه و مجله در ستون و سطرآنچنان که لازم است، و برای شرایط فعلی تکنولوژی مورد نیاز، و کاربردهای متنوع با هدف بهبود ابزارهای کاربردی می باشد، کتابهای زیادی در شصت و سه درصد گذشته حال و آینده، شناخت فراوان جامعه و متخصصان را می طلبد، تا با نرم افزارها شناخت بیشتری را برای طراحان رایانه ای علی الخصوص طراحان خلاقی، و فرهنگ پیشرو در زبان فارسی ایجاد کرد، در این صورت می توان امید داشت که تمام و دشواری موجود در ارائه راهکارها، و شرایط سخت تایپ به پایان رسد و زمان مورد نیاز شامل حروفچینی دستاوردهای اصلی، و جوابگوی سوالات پیوسته اهل دنیای موجود طراحی اساسا مورد استفاده قرار گیرد.\""
        else ->"\"متن ساختگی\""
    }

}

 fun getLongValue(type: NumberInputType?): Long {
    return when (type) {
        NumberInputType.low -> 1000
        NumberInputType.mid -> 1000000
        NumberInputType.high -> 1000000000000
        else -> 1000
    }

}

 fun getIntValue(type: NumberInputType?): Int {
    return when (type) {
        NumberInputType.low -> 10
        NumberInputType.mid -> 100
        NumberInputType.high -> 1000000
        else -> 10
    }

}

 fun getDoubleValue(type: NumberInputType?): Double {
    return when (type) {
        NumberInputType.low -> 10.0
        NumberInputType.mid -> 100.0
        NumberInputType.high -> 1000000.0
        else -> 10.0
    }

}

 fun getFloatValue(type: NumberInputType?): Float {
    return when (type) {
        NumberInputType.low -> 10.0f
        NumberInputType.mid -> 100.0f
        NumberInputType.high -> 1000000.0f
        else -> 10.0f
    }
}