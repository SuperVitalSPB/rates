package ru.supervital.rates;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RateArrayAdapter extends ArrayAdapter <Rate> {
	public static final String TAG = "rates.RateArrayAdapter";
	
    private final Activity context;
    private final ArrayList<Rate> rates;
    View.OnTouchListener mTouchListener;
    
    public RateArrayAdapter(Activity context, ArrayList<Rate> rates, View.OnTouchListener listener) {
        super(context, R.layout.rowlayout, rates);
        mTouchListener = listener;
        this.context = context;
        this.rates = rates;
    }

// ����� ��� ���������� �� ������� ����� � ��� ����������� �������
// �� �������� ������
    static class ViewHolder {
        public ImageView Icon;
        public TextView txtCode;
        public TextView txtRate;
        public TextView txtNominal;
        public TextView txtName;
        public ImageView RCh;        
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ViewHolder ������������ ������ ��������� ����� ������� ��������
   	
        ViewHolder holder;
        // ������� ������������ ������, ���� �������� �����
        // �������� ������ ���� ������� ������ ��� ���� ������� ���� � ��� ��
        View rowView = convertView;
        
        
        
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.rowlayout, null, true);
            holder = new ViewHolder();
            holder.Icon =  (ImageView) rowView.findViewById(R.id.icon);
            holder.txtCode =    (TextView) rowView.findViewById(R.id.txtCode);
            holder.txtRate =    (TextView) rowView.findViewById(R.id.txtRate);
            holder.txtNominal = (TextView) rowView.findViewById(R.id.txtNominal);
            holder.RCh = (ImageView) rowView.findViewById(R.id.rch);            
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        holder.txtCode.setText((CharSequence) rates.get(position).Code);
        holder.txtRate.setText((CharSequence) String.valueOf(rates.get(position).Rate));
        holder.txtNominal.setText((CharSequence) rates.get(position).Nominal);
        
        Rate rate = rates.get(position);
        
        int ident = context.getResources().getIdentifier(rate.Code.toLowerCase(new Locale("ru")),
        												"drawable", 
        												"ru.supervital.rates");
        if (ident != 0) {
        	holder.Icon.setImageResource(ident);
        } else holder.Icon.setImageResource(R.drawable.ic_android);

        if (rate.isPrevLoaded) {
	        if (rate.Rate > rate.RatePrev) 
	        	holder.RCh.setImageResource(R.drawable.vdown); 
	        else if (rate.Rate < rate.RatePrev) 
	        	holder.RCh.setImageResource(R.drawable.vup);
        }
        
        rowView.setOnTouchListener(mTouchListener);
        
        return rowView;
    }
 
    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
}


// setOnItemLongClickListener(new View.OnItemLongClickListener) ����������� ����